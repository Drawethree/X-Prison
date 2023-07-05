package dev.drawethree.xprison.autosell.manager;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.api.events.XPrisonAutoSellEvent;
import dev.drawethree.xprison.autosell.api.events.XPrisonSellAllEvent;
import dev.drawethree.xprison.autosell.model.AutoSellItemStack;
import dev.drawethree.xprison.autosell.model.SellRegion;
import dev.drawethree.xprison.autosell.utils.AutoSellContants;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.economy.EconomyUtils;
import dev.drawethree.xprison.utils.inventory.InventoryUtils;
import dev.drawethree.xprison.utils.misc.MaterialUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoSellManager {

    private static final CooldownMap<Player> INVENTORY_FULL_COOLDOWN_MAP = CooldownMap.create(Cooldown.of(2, TimeUnit.SECONDS));

    private final XPrisonAutoSell plugin;
    private final Map<UUID, Double> lastEarnings;
    private final Map<UUID, Long> lastItems;
    private final Map<String, SellRegion> regionsAutoSell;
    private final List<UUID> enabledAutoSellPlayers;
    private final Map<String, Set<String>> notLoadedSellRegions;

    public AutoSellManager(XPrisonAutoSell plugin) {
        this.plugin = plugin;
        this.enabledAutoSellPlayers = new ArrayList<>();
        this.lastEarnings = new HashMap<>();
        this.lastItems = new HashMap<>();
        this.regionsAutoSell = new HashMap<>();
        this.notLoadedSellRegions = new HashMap<>();
    }


    private void loadAutoSellRegions() {
        this.regionsAutoSell.clear();

        YamlConfiguration configuration = this.plugin.getAutoSellConfig().getYamlConfig();

        ConfigurationSection section = configuration.getConfigurationSection("regions");

        if (section == null) {
            return;
        }

        for (String regName : section.getKeys(false)) {
            this.loadSellRegionFromConfig(configuration, regName);
        }
    }

    private boolean loadSellRegionFromConfig(YamlConfiguration config, String regionName) {

        String worldName = config.getString("regions." + regionName + ".world");

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            this.plugin.getCore().getLogger().warning("There is no such World named " + worldName + "! Perhaps its no loaded yet?");
            this.plugin.getCore().getLogger().warning("Postponing loading of region " + regionName + ".");
            this.postponeLoadingOfSellRegion(worldName, regionName);
            return false;
        }

        IWrappedRegion region = this.validateWrappedRegion(regionName, world);

        if (region == null) {
            this.plugin.getCore().getLogger().warning("There is no such WorldGuard region named " + regionName + " in world " + world.getName());
            return false;
        }

        String permRequired = config.getString("regions." + regionName + ".permission");

        Map<CompMaterial, Double> sellPrices = this.loadSellPricesForRegion(config, regionName);

        SellRegion sellRegion = new SellRegion(region, world, permRequired, sellPrices);
        this.regionsAutoSell.put(regionName, sellRegion);

        this.plugin.getCore().getLogger().info("Loaded auto-sell region named '" + regionName + "'");
        return true;
    }

    private void postponeLoadingOfSellRegion(String worldName, String regionName) {
        Set<String> currentlyPostponed = this.notLoadedSellRegions.getOrDefault(worldName, new HashSet<>());
        currentlyPostponed.add(regionName);
        this.notLoadedSellRegions.put(worldName, currentlyPostponed);
    }

    private IWrappedRegion validateWrappedRegion(String regionName, World world) {
        Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(world, regionName);
        return optRegion.orElse(null);
    }

    private Map<CompMaterial, Double> loadSellPricesForRegion(YamlConfiguration config, String regionName) {
        Map<CompMaterial, Double> sellPrices = new HashMap<>();

        for (String item : config.getConfigurationSection("regions." + regionName + ".items").getKeys(false)) {
            CompMaterial type = CompMaterial.valueOf(item);
            double sellPrice = config.getDouble("regions." + regionName + ".items." + item);
            sellPrices.put(type, sellPrice);
        }
        return sellPrices;
    }

    public void reload() {
        this.load();
    }

    public void load() {
        this.loadAutoSellRegions();
    }


    public void sellAll(Player sender, IWrappedRegion region) {

        if (!this.validateRegionBeforeSellAll(sender, region)) {
            return;
        }

        this.plugin.getCore().debug("User " + sender.getName() + " ran /sellall in region " + region.getId(), this.plugin);

        SellRegion sellRegion = regionsAutoSell.get(region.getId());

        if (!checkIfPlayerCanSellInSellRegion(sender, sellRegion)) {
            return;
        }

        Map<AutoSellItemStack, Double> itemsToSell = sellRegion.previewInventorySell(sender);

        XPrisonSellAllEvent event = this.callSellAllEvent(sender, sellRegion, itemsToSell);

        if (event.isCancelled()) {
            return;
        }

        itemsToSell = event.getItemsToSell();

        double totalAmount = this.sellItems(sender, itemsToSell);

        itemsToSell.keySet().forEach(item -> sender.getInventory().remove(item.getItemStack()));

        if (totalAmount > 0.0) {
            PlayerUtils.sendMessage(sender, this.plugin.getAutoSellConfig().getMessage("sell_all_complete").replace("%price%", String.format("%,.0f", totalAmount)));
        }
    }

    private double sellItems(Player player, Map<AutoSellItemStack, Double> itemsToSell) {

        double totalAmount = itemsToSell.values().stream().mapToDouble(Double::doubleValue).sum();

        if (this.plugin.isMultipliersModuleEnabled()) {
            totalAmount = (long) this.plugin.getCore().getMultipliers().getApi().getTotalToDeposit(player, totalAmount, MultiplierType.SELL);
        }

        EconomyUtils.deposit(player, totalAmount);
        return totalAmount;
    }

    private XPrisonSellAllEvent callSellAllEvent(Player sender, SellRegion sellRegion, Map<AutoSellItemStack, Double> sellItems) {
        XPrisonSellAllEvent event = new XPrisonSellAllEvent(sender, sellRegion, sellItems);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("XPrisonSellAllEvent was cancelled.", this.plugin);
        }

        return event;
    }

    private XPrisonAutoSellEvent callAutoSellEvent(Player player, SellRegion sellRegion, Map<AutoSellItemStack, Double> itemsToSell) {
        XPrisonAutoSellEvent event = new XPrisonAutoSellEvent(player, sellRegion, itemsToSell);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("XPrisonAutoSellEvent was cancelled.", this.plugin);
        }

        return event;
    }

    private boolean checkIfPlayerCanSellInSellRegion(Player sender, SellRegion sellRegion) {
        if (!sellRegion.canPlayerSellInRegion(sender)) {
            PlayerUtils.sendMessage(sender, this.plugin.getAutoSellConfig().getMessage("no_permission_sell").replace("%perm%", sellRegion.getPermissionRequired()));
            return false;
        }
        return true;
    }

    private boolean validateRegionBeforeSellAll(Player sender, IWrappedRegion region) {

        if (region == null) {
            PlayerUtils.sendMessage(sender, this.plugin.getAutoSellConfig().getMessage("not_in_region"));
            return false;
        }

        return isAutoSellRegion(region);
    }

    private boolean isAutoSellRegion(IWrappedRegion region) {
        return regionsAutoSell.containsKey(region.getId());
    }

    public void resetLastEarnings() {
        this.lastEarnings.clear();
    }

    public void resetLastItems() {
        this.lastItems.clear();
    }

    public double getPlayerLastEarnings(Player p) {
        return this.lastEarnings.getOrDefault(p.getUniqueId(), 0.0D);
    }

    public long getPlayerLastItemsAmount(Player p) {
        return this.lastItems.getOrDefault(p.getUniqueId(), 0L);
    }

    public double getCurrentEarnings(Player player) {
        return lastEarnings.getOrDefault(player.getUniqueId(), 0.0);
    }

    public double getPriceForItem(String regionName, ItemStack item) {
        SellRegion region = regionsAutoSell.get(regionName);
        if (region != null) {
            return region.getPriceForItem(item);
        }
        return 0.0;
    }

    public boolean hasAutoSellEnabled(Player p) {
        return enabledAutoSellPlayers.contains(p.getUniqueId());
    }

    public Collection<SellRegion> getAutoSellRegions() {
        return this.regionsAutoSell.values();
    }

    public SellRegion getAutoSellRegion(Location location) {
        for (SellRegion region : this.regionsAutoSell.values()) {
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }

    public void toggleAutoSell(Player player) {
        boolean removed = enabledAutoSellPlayers.remove(player.getUniqueId());

        if (removed) {
            PlayerUtils.sendMessage(player, this.plugin.getAutoSellConfig().getMessage("autosell_disable"));
        } else {
            PlayerUtils.sendMessage(player, this.plugin.getAutoSellConfig().getMessage("autosell_enable"));
            enabledAutoSellPlayers.add(player.getUniqueId());
        }
    }

    public boolean canPlayerEnableAutosellOnJoin(Player player) {
        if (!this.plugin.getAutoSellConfig().isEnableAutosellAutomatically()) {
            return false;
        }
        return player.hasPermission(AutoSellContants.AUTOSELL_PERMISSION) && !hasAutoSellEnabled(player);
    }

    public SellRegion getSellRegionFromWrappedRegion(IWrappedRegion region) {
        return regionsAutoSell.getOrDefault(region.getId(), null);
    }

    public void updateSellRegion(SellRegion sellRegion) {
        this.regionsAutoSell.put(sellRegion.getRegion().getId(), sellRegion);
    }

    public boolean givePlayerItem(Player player, Block block) {

        if (!InventoryUtils.hasSpace(player.getInventory())) {
            this.notifyInventoryFull(player);
            return true;
        }

        player.getInventory().addItem(createItemStackToGive(player, block));
        return true;
    }

    private ItemStack createItemStackToGive(Player player, Block block) {
        int amount = EnchantUtils.getFortuneBlockCount(player.getItemInHand(), block);

        ItemStack toGive;

        if (this.plugin.getAutoSellConfig().isAutoSmelt()) {
            toGive = MaterialUtils.getSmeltedFormAsItemStack(block);
        } else {
            toGive = CompMaterial.fromBlock(block).toItem();
        }
        toGive.setAmount(amount);
        return toGive;
    }

    private void notifyInventoryFull(Player player) {

        if (!this.plugin.getAutoSellConfig().isInventoryFullNotificationEnabled() || !INVENTORY_FULL_COOLDOWN_MAP.test(player)) {
            return;
        }

        List<String> inventoryFullTitle = this.plugin.getAutoSellConfig().getInventoryFullNotificationTitle();
        String inventoryFullNotificationMessage = this.plugin.getAutoSellConfig().getInventoryFullNotificationMessage();

        if (!inventoryFullTitle.isEmpty()) {
            PlayerUtils.sendTitle(player, inventoryFullTitle.get(0), inventoryFullTitle.get(1));
        } else {
            PlayerUtils.sendMessage(player, inventoryFullNotificationMessage);
        }
    }

    public boolean autoSellBlock(Player player, Block block) {

        SellRegion sellRegion = this.getAutoSellRegion(block.getLocation());

        if (sellRegion == null) {
            return false;
        }

        Map<AutoSellItemStack, Double> itemsToSell = sellRegion.previewItemsSell(Arrays.asList(createItemStackToGive(player, block)));

        XPrisonAutoSellEvent event = this.callAutoSellEvent(player, sellRegion, itemsToSell);

        if (event.isCancelled()) {
            return false;
        }

        itemsToSell = event.getItemsToSell();

        int amountOfItems = itemsToSell.keySet().stream().mapToInt(item -> item.getItemStack().getAmount()).sum();
        double moneyEarned = this.sellItems(player, itemsToSell);

        this.updateCurrentEarningsAndLastItems(player, moneyEarned, amountOfItems);

        return true;
    }

    private void updateCurrentEarningsAndLastItems(Player player, double moneyEarned, int amountOfItems) {
        this.addToCurrentEarnings(player, moneyEarned);
        this.addToLastItems(player, amountOfItems);
    }

    public void addToCurrentEarnings(Player player, double amount) {
        double current = this.lastEarnings.getOrDefault(player.getUniqueId(), 0.0);
        this.lastEarnings.put(player.getUniqueId(), current + amount);
    }

    public void addToLastItems(Player player, int amountOfItems) {
        long current = this.lastItems.getOrDefault(player.getUniqueId(), 0L);
        this.lastItems.put(player.getUniqueId(), current + amountOfItems);
    }

    public double getPriceForBlock(String regionName, Block block) {
        CompMaterial material = CompMaterial.fromBlock(block);
        SellRegion region = regionsAutoSell.get(regionName);
        if (region != null) {
            return region.getSellPriceForMaterial(material);
        }
        return 0.0;
    }

    public double getPriceForBlock(Block block) {
        CompMaterial material = CompMaterial.fromBlock(block);
        SellRegion region = getAutoSellRegion(block.getLocation());
        if (region != null) {
            return region.getSellPriceForMaterial(material);
        }
        return 0.0;
    }

    public void loadPostponedAutoSellRegions(World world) {
        YamlConfiguration configuration = this.plugin.getAutoSellConfig().getYamlConfig();
        Set<String> regionNames = this.notLoadedSellRegions.getOrDefault(world.getName(), new HashSet<>());
        regionNames.removeIf(regionName -> this.loadSellRegionFromConfig(configuration, regionName));
        this.notLoadedSellRegions.put(world.getName(), regionNames);
    }

    public SellRegion getSellRegionByName(String name) {
        return regionsAutoSell.get(name);
    }

    public void sellBlocks(Player player, List<Block> blocks) {
        blocks.forEach(block -> autoSellBlock(player, block));
    }
}
