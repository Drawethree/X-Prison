package dev.drawethree.xprison.autosell.manager;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.api.autosell.events.XPrisonAutoSellEvent;
import dev.drawethree.xprison.api.autosell.events.XPrisonSellAllEvent;
import dev.drawethree.xprison.api.autosell.model.AutoSellItemStack;
import dev.drawethree.xprison.api.multipliers.model.MultiplierType;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.AutoSellItemStackImpl;
import dev.drawethree.xprison.autosell.utils.AutoSellContants;
import dev.drawethree.xprison.autosell.utils.SellPriceComparator;
import dev.drawethree.xprison.enchants.utils.EnchantUtils;
import dev.drawethree.xprison.utils.economy.EconomyUtils;
import dev.drawethree.xprison.utils.inventory.InventoryUtils;
import dev.drawethree.xprison.utils.log.XPrisonLogger;
import dev.drawethree.xprison.utils.misc.MaterialUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoSellManager {

    private static final CooldownMap<Player> INVENTORY_FULL_COOLDOWN_MAP = CooldownMap.create(Cooldown.of(2, TimeUnit.SECONDS));

    private final XPrisonAutoSell plugin;
    private final Map<UUID, Double> lastEarnings;
    private final Map<UUID, Long> lastItems;
    private final List<UUID> enabledAutoSellPlayers;
    private final Map<XMaterial, Double> sellPrices;

    public AutoSellManager(XPrisonAutoSell plugin) {
        this.plugin = plugin;
        this.enabledAutoSellPlayers = new ArrayList<>();
        this.lastEarnings = new HashMap<>();
        this.lastItems = new HashMap<>();
        this.sellPrices = new HashMap<>();
    }

    public void reload() {
        this.load();
    }

    public void load() {
        this.loadSellPrices();
    }

    private void loadSellPrices() {
        this.sellPrices.clear();
        ConfigurationSection section = this.plugin.getAutoSellConfig().getYamlConfig().getConfigurationSection("sell-prices");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            double price = section.getDouble(key);
            this.sellPrices.put(XMaterial.matchXMaterial(key).get(), price);
            XPrisonLogger.info(String.format("Loaded Sell price %s %,.2f,",key, price));
        }
    }

    public void saveSellPrices() {
        this.plugin.getAutoSellConfig().getConfig().set("sell-prices", null);
        for (XMaterial material : sellPrices.keySet()) {
            double sellPrice = sellPrices.get(material);
            if (sellPrice <= 0.0) {
                continue;
            }
            this.plugin.getAutoSellConfig().getConfig().set("sell-prices." + material.name(), sellPrice);
        }
        this.plugin.getAutoSellConfig().getConfig().save();
    }


    public void sellAll(Player sender) {

        this.plugin.getCore().debug("User " + sender.getName() + " ran /sellall", this.plugin);

        Map<AutoSellItemStack, Double> itemsToSell = previewInventorySell(sender);

        XPrisonSellAllEvent event = this.callSellAllEvent(sender, itemsToSell);

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

    private XPrisonSellAllEvent callSellAllEvent(Player sender, Map<AutoSellItemStack, Double> sellItems) {
        XPrisonSellAllEvent event = new XPrisonSellAllEvent(sender, sellItems);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("XPrisonSellAllEvent was cancelled.", this.plugin);
        }

        return event;
    }

    private XPrisonAutoSellEvent callAutoSellEvent(Player player, Map<AutoSellItemStack, Double> itemsToSell) {
        XPrisonAutoSellEvent event = new XPrisonAutoSellEvent(player, itemsToSell);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("XPrisonAutoSellEvent was cancelled.", this.plugin);
        }

        return event;
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

    public double getPriceForItem(ItemStack item) {
        XMaterial material = XMaterial.matchXMaterial(item);
        return item.getAmount() * this.sellPrices.getOrDefault(material, 0.0);
    }

    public boolean hasAutoSellEnabled(Player p) {
        return enabledAutoSellPlayers.contains(p.getUniqueId());
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
            toGive = XMaterial.matchXMaterial(block.getType()).parseItem();
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

        Map<AutoSellItemStack, Double> itemsToSell = previewItemsSell(Arrays.asList(createItemStackToGive(player, block)));

        XPrisonAutoSellEvent event = this.callAutoSellEvent(player, itemsToSell);

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

    public double getPriceForBlock(Block block) {
        XMaterial material = XMaterial.matchXMaterial(block.getType());
        return getSellPriceForMaterial(material);
    }

    public void sellBlocks(Player player, List<Block> blocks) {
        blocks.forEach(block -> autoSellBlock(player, block));
    }

    public Map<AutoSellItemStack, Double> previewItemsSell(Collection<ItemStack> items) {

        Map<AutoSellItemStack, Double> itemsToSell = new HashMap<>();

        for (ItemStack item : items) {

            if (item == null) {
                continue;
            }

            double priceForItem = this.getPriceForItem(item);

            if (priceForItem <= 0.0) {
                continue;
            }

            itemsToSell.put(new AutoSellItemStackImpl(item), priceForItem);
        }

        return itemsToSell;
    }

    public Map<AutoSellItemStack, Double> previewInventorySell(Player player) {
        return previewItemsSell(Arrays.asList(player.getInventory().getContents()));
    }

    public void addSellPrice(XMaterial material, double price) {
        this.sellPrices.put(material, price);
        this.saveSellPrices();
    }

    public boolean sellsMaterial(XMaterial material) {
        return this.sellPrices.containsKey(material);
    }

    public void removeSellPrice(XMaterial material) {
        this.sellPrices.remove(material);
        this.saveSellPrices();
    }

    public Set<XMaterial> getSellingMaterials() {
        return this.sellPrices.keySet().stream().sorted(new SellPriceComparator(this)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public double getSellPriceForMaterial(XMaterial material) {
        return sellPrices.getOrDefault(material, 0.0);
    }
}
