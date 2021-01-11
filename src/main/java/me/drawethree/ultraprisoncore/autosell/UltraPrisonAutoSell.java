package me.drawethree.ultraprisoncore.autosell;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonAutoSellEvent;
import me.drawethree.ultraprisoncore.autosell.api.UltraPrisonAutoSellAPI;
import me.drawethree.ultraprisoncore.autosell.api.UltraPrisonAutoSellAPIImpl;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class UltraPrisonAutoSell implements UltraPrisonModule {


    private static final String ADMIN_PERMISSION = "ultraprison.autosell.admin";
    private static final String AUTOSELL_TOGGLE_PERMISSION = "ultraprison.autosell.toggle";

    @Getter
    private FileManager.Config config;

    private HashMap<String, AutoSellRegion> regionsAutoSell;
    private HashMap<String, String> messages;
    private List<String> autoSellBroadcastMessage;
    private long broadcastTime;
    private HashMap<UUID, Double> lastEarnings;
    private HashMap<UUID, Long> lastItems;
    @Getter
    private UltraPrisonAutoSellAPI api;
    private List<UUID> disabledAutoSell;
    @Getter
    private UltraPrisonCore core;
    private boolean enabled;

    public UltraPrisonAutoSell(UltraPrisonCore UltraPrisonCore) {
        this.core = UltraPrisonCore;
        this.config = UltraPrisonCore.getFileManager().getConfig("autosell.yml").copyDefaults(true).save();
        this.disabledAutoSell = new ArrayList<>();
        this.lastEarnings = new HashMap<>();
        this.lastItems = new HashMap<>();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(this.getConfig().get().getString("messages." + key)));
        }
    }

    private void loadAutoSellRegions() {
        regionsAutoSell = new HashMap<>();
        for (String regName : this.getConfig().get().getConfigurationSection("regions").getKeys(false)) {

            String worldName = this.getConfig().get().getString("regions." + regName + ".world");

            String permRequired = this.getConfig().get().getString("regions." + regName + ".permission");

            World w = Bukkit.getWorld(worldName);

            if (w == null) {
                this.core.getLogger().warning("There is no such World named " + worldName + "! Perhaps its no loaded yet?");
                continue;
            }

            Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(w, regName);

            if (!optRegion.isPresent()) {
                this.core.getLogger().warning("There is no such WorldGuard region named " + regName + " in world " + worldName);
                continue;
            }

            IWrappedRegion region = optRegion.get();

            if (region == null || w == null) {
                continue;
            }

            Map<Material, Double> sellPrices = new HashMap<>();

            for (String item : this.getConfig().get().getConfigurationSection("regions." + regName + ".items").getKeys(false)) {
                Material type = Material.valueOf(item);
                double sellPrice = this.getConfig().get().getDouble("regions." + regName + ".items." + item);
                sellPrices.put(type, sellPrice);
            }


            regionsAutoSell.put(regName, new AutoSellRegion(region,permRequired,sellPrices));

            this.core.getLogger().info("Loaded auto-sell region named " + regName + " in world " + worldName);

        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.config = this.core.getFileManager().getConfig("autosell.yml");
        this.config.reload();
    }

    @Override
    public void enable() {
        this.enabled = true;
        this.loadAutoSellRegions();
        this.broadcastTime = this.getConfig().get().getInt("auto_sell_broadcast.time");
        this.autoSellBroadcastMessage = this.getConfig().get().getStringList("auto_sell_broadcast.message");
        this.loadMessages();
        this.registerCommands();
        this.registerListeners();
        this.runBroadcastTask();
        this.api = new UltraPrisonAutoSellAPIImpl(this);
    }

    private void runBroadcastTask() {
        Schedulers.async().runRepeating(() -> {
            Players.all().stream().filter(p -> lastEarnings.containsKey(p.getUniqueId())).forEach(p -> {
                double lastAmount = lastEarnings.getOrDefault(p.getUniqueId(), 0.0);
                long lastItems = this.lastItems.getOrDefault(p.getUniqueId(), 0L);
                for (String s : this.autoSellBroadcastMessage) {
                    p.sendMessage(Text.colorize(s.replace("%money%", String.format("%,.0f", lastAmount)).replace("%items%", String.format("%,d", lastItems))));
                }
            });
            lastEarnings.clear();
        }, this.broadcastTime, TimeUnit.SECONDS, this.broadcastTime, TimeUnit.SECONDS);
    }

    private void registerListeners() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().runLater(() -> {
                    if (!disabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                        e.getPlayer().sendMessage(getMessage("autosell_enable"));
                    }
                }, 20));
        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !e.isCancelled() && e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial())
                .handler(e -> {
                    int fortuneLevel = core.getEnchants().getApi().getEnchantLevel(e.getPlayer().getItemInHand(), 3);
                    if (disabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                        e.getPlayer().getInventory().addItem(new ItemStack(e.getBlock().getType(), 1 + fortuneLevel));

                        e.getBlock().getDrops().clear();
                        e.getBlock().setType(CompMaterial.AIR.getMaterial());
                    } else {
                        Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation());

                        if (regions == null || regions.isEmpty()) {
                            return;
                        }

                        for (IWrappedRegion reg : regions) {
                            if (regionsAutoSell.containsKey(reg.getId()) && regionsAutoSell.get(reg.getId()).sellsMaterial(e.getBlock().getType())) {
                                int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
                                double amount = core.getMultipliers().getApi().getTotalToDeposit(e.getPlayer(), (regionsAutoSell.get(reg.getId()).getSellPriceFor(e.getBlock().getType()) + 0.0) * amplifier);

                                UltraPrisonAutoSellEvent event = new UltraPrisonAutoSellEvent(e.getPlayer(), reg, e.getBlock(), amount);

                                Events.call(event);

                                if (event.isCancelled()) {
                                    return;
                                }

                                amount = event.getMoneyToDeposit();

                                int amountOfItems = 0;

                                for (ItemStack item : e.getBlock().getDrops(e.getPlayer().getItemInHand())) {
                                    amountOfItems += item.getAmount() * amplifier;
                                }

                                boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

                                core.getEconomy().depositPlayer(e.getPlayer(), luckyBooster ? amount * 2 : amount);
                                core.getAutoSell().addToCurrentEarnings(e.getPlayer(), luckyBooster ? amount * 2 : amount);

                                this.lastItems.put(e.getPlayer().getUniqueId(), this.lastItems.getOrDefault(e.getPlayer().getUniqueId(), 0L) + amountOfItems);

                                e.getBlock().getDrops().clear();
                                e.getBlock().setType(CompMaterial.AIR.toMaterial());
                                break;
                            }
                        }
                    }

                }).bindWith(core);
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public String getName() {
        return "Auto Sell";
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .assertPermission(AUTOSELL_TOGGLE_PERMISSION, getMessage("no_permission_autosell_toggle"))
                .handler(c -> {
                    if (c.args().size() == 0) {
                        toggleAutoSell(c.sender());
                    }
                }).registerAndBind(core, "autosell");
        Commands.create()
                .assertPlayer()
                .assertPermission(ADMIN_PERMISSION)
                .handler(c -> {
                    if (c.args().size() == 1) {

                        if (c.sender().getItemInHand() == null) {
                            c.sender().sendMessage(Text.colorize("&cPlease hold some item!"));
                            return;
                        }

                        double price = c.arg(0).parseOrFail(Double.class).doubleValue();
                        Material type = c.sender().getItemInHand().getType();
                        IWrappedRegion region = getFirstRegionAtLocation(c.sender().getLocation());

                        if (region == null) {
                            c.sender().sendMessage(Text.colorize("&cYou must be standing in a region!"));
                            return;
                        }

                        getConfig().set("regions." + region.getId() + ".world", c.sender().getWorld().getName());
                        getConfig().set("regions." + region.getId() + ".items." + type.name(), price);
                        getConfig().save();

                        if (type == Material.REDSTONE_ORE) {
                            getConfig().set("regions." + region.getId() + ".world", c.sender().getWorld().getName());
                            getConfig().set("regions." + region.getId() + ".items." + Material.GLOWING_REDSTONE_ORE.name(), price);
                            getConfig().save();
                        }

                        AutoSellRegion autoSellRegion;

                        if (regionsAutoSell.containsKey(region.getId())) {
                            autoSellRegion = regionsAutoSell.get(region.getId());
                        } else {
                            autoSellRegion = new AutoSellRegion(region,null,new HashMap<>());
                        }

                        if (type == Material.REDSTONE_ORE) {
                            autoSellRegion.addSellPrice(Material.GLOWING_REDSTONE_ORE, price);
                        }

                        autoSellRegion.addSellPrice(type, price);
                        regionsAutoSell.put(region.getId(), autoSellRegion);

                        c.sender().sendMessage(Text.colorize(String.format("&aSuccessfuly set sell price of &e%s &ato &e$%.2f &ain region &e%s", type.name(), price, region.getId())));

                    }
                }).registerAndBind(core, "sellprice");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0 || c.args().size() == 1) {

                        Set<IWrappedRegion> regions;

                        if (c.args().size() == 1) {

                            String regionName = c.rawArg(0);

                            Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(c.sender().getLocation().getWorld(), regionName);

                            if (!optRegion.isPresent()) {
                                c.sender().sendMessage(getMessage("invalid_region"));
                                return;
                            }

                            regions = new HashSet<>();
                            regions.add(optRegion.get());

                        } else {
                            regions = WorldGuardWrapper.getInstance().getRegions(c.sender().getLocation());
                        }

                        if (regions == null || regions.isEmpty()) {
                            c.sender().sendMessage(getMessage("not_in_region"));
                            return;
                        }

                        for (IWrappedRegion region : regions) {
                            if (regionsAutoSell.containsKey(region.getId())) {

                                AutoSellRegion autoSellRegion = regionsAutoSell.get(region.getId());

                                if (autoSellRegion.getPermissionRequired() != null && !autoSellRegion.getPermissionRequired().isEmpty() && !c.sender().hasPermission(autoSellRegion.getPermissionRequired())) {
                                    c.sender().sendMessage(getMessage("no_permission_sell").replace("%perm%", autoSellRegion.getPermissionRequired()));
                                    return;
                                }

                                double totalPrice = 0;

                                List<ItemStack> toRemove = new ArrayList<>();

                                for (Material m : autoSellRegion.getSellingMaterials()) {
                                    for (ItemStack item : Arrays.stream(c.sender().getInventory().getContents()).filter(i -> i != null && i.getType() == m).collect(Collectors.toList())) {
                                        totalPrice += item.getAmount() * autoSellRegion.getSellPriceFor(m);
                                        toRemove.add(item);
                                    }
                                }

                                toRemove.forEach(i -> c.sender().getInventory().removeItem(i));
                                totalPrice = (long) core.getMultipliers().getApi().getTotalToDeposit(c.sender(), totalPrice);
                                core.getEconomy().depositPlayer(c.sender(), totalPrice);
                                c.sender().sendMessage(getMessage("sell_all_complete").replace("%price%", String.format("%,.0f", totalPrice)));
                                break;
                            }
                        }
                    }
                }).registerAndBind(core, "sellall");


    }

    private void toggleAutoSell(Player player) {
        if (disabledAutoSell.contains(player.getUniqueId())) {
            player.sendMessage(getMessage("autosell_enable"));
            disabledAutoSell.remove(player.getUniqueId());
        } else {
            disabledAutoSell.add(player.getUniqueId());
            player.sendMessage(getMessage("autosell_disable"));
        }
    }

    private IWrappedRegion getFirstRegionAtLocation(Location loc) {
        Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc);
        return regions.size() == 0 ? null : regions.iterator().next();
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    public double getCurrentEarnings(Player player) {
        return lastEarnings.containsKey(player.getUniqueId()) ? lastEarnings.get(player.getUniqueId()) : 0.0;
    }

    public double getPriceForBrokenBlock(IWrappedRegion region, Block block) {
        return regionsAutoSell.containsKey(region.getId()) ? regionsAutoSell.get(region.getId()).getSellPriceFor(block.getType()) : 0.0;
    }

    public boolean hasAutoSellEnabled(Player p) {
        return !disabledAutoSell.contains(p.getUniqueId());
    }

    public void addToCurrentEarnings(Player p, double amount) {
        double current = this.lastEarnings.getOrDefault(p.getUniqueId(), 0.0);

        this.lastEarnings.put(p.getUniqueId(), current + amount);
    }

}
