package me.drawethree.wildprisoncore.autosell;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.autosell.api.WildPrisonAutoSellAPI;
import me.drawethree.wildprisoncore.autosell.api.WildPrisonAutoSellAPIImpl;
import me.drawethree.wildprisoncore.config.FileManager;
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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class WildPrisonAutoSell {

    @Getter
    private FileManager.Config config;

    private HashMap<ProtectedRegion, HashMap<Material, Integer>> regionsAutoSell;
    private HashMap<String, String> messages;
    private HashMap<UUID, Long> lastMinuteEarnings;
    @Getter
    private WildPrisonAutoSellAPI api;
    private List<UUID> disabledAutoSell;
    @Getter
    private WildPrisonCore core;

    public WildPrisonAutoSell(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("autosell.yml").copyDefaults(true).save();
        this.api = new WildPrisonAutoSellAPIImpl(this);
        this.disabledAutoSell = new ArrayList<>();
        this.lastMinuteEarnings = new HashMap<>();
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

            World w = Bukkit.getWorld(worldName);
            ProtectedRegion region = WorldGuardPlugin.inst().getRegionManager(w).getRegion(regName);

            if (region == null || w == null) {
                continue;
            }

            HashMap<Material, Integer> sellPrices = new HashMap<>();
            for (String item : this.getConfig().get().getConfigurationSection("regions." + regName + ".items").getKeys(false)) {
                Material type = Material.valueOf(item);
                int sellPrice = this.getConfig().get().getInt("regions." + regName + ".items." + item);
                sellPrices.put(type, sellPrice);
            }
            regionsAutoSell.put(region, sellPrices);
        }
    }

    public void enable() {
        this.loadAutoSellRegions();
        this.loadMessages();
        this.registerCommands();
        this.registerListeners();
        this.runBroadcastTask();
    }

    private void runBroadcastTask() {
        Schedulers.async().runRepeating(() -> {
            /*for (Player p : lastMinuteEarnings.keySet()) {
                if (lastMinuteEarnings.get(p) > 0) {
                    p.sendMessage(getMessage("last_minute_earn").replace("%amount%", String.format("%,d", lastMinuteEarnings.get(p))));
                }
            }
            lastMinuteEarnings.clear();*/
            HashMap<UUID, Long> temp = new HashMap<>();
            Players.all().stream().filter(p -> lastMinuteEarnings.containsKey(p.getUniqueId())).forEach(p -> {
                long lastAmount = lastMinuteEarnings.get(p.getUniqueId());
                long currentAmount = (long) core.getEconomy().getBalance(p);
                if (currentAmount > lastAmount) {
                    p.sendMessage(getMessage("last_minute_earn").replace("%amount%", String.format("%,d", currentAmount - lastAmount)));
                }
                temp.put(p.getUniqueId(), currentAmount);
            });
            lastMinuteEarnings.clear();
            lastMinuteEarnings = temp;
        }, 0, TimeUnit.SECONDS, 1, TimeUnit.MINUTES);
    }

    private void registerListeners() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    Schedulers.async().runLater(() -> {
                        if (!disabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                            e.getPlayer().sendMessage(getMessage("autosell_enable"));
                        }
                    }, 20);
                });
        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !e.isCancelled() && e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE)
                .handler(e -> {
                    int fortuneLevel = core.getEnchants().getApi().getEnchantLevel(e.getPlayer(), 3);
                    if (disabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                        if (e.getBlock().getType() != Material.ENDER_STONE && e.getBlock().getType() != Material.OBSIDIAN) {
                            e.getPlayer().getInventory().addItem(new ItemStack(e.getBlock().getType(), 1 + fortuneLevel));
                        }
                        e.getBlock().setType(Material.AIR);
                    } else {
                        ProtectedRegion reg = getFirstRegionAtLocation(e.getBlock().getLocation());

                        if (reg == null) {
                            return;
                        }

                        if (regionsAutoSell.containsKey(reg) && regionsAutoSell.get(reg).containsKey(e.getBlock().getType())) {
                            int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
                            int amount = (int) core.getMultipliers().getApi().getTotalToDeposit(e.getPlayer(), regionsAutoSell.get(reg).get(e.getBlock().getType()) * amplifier);

                            core.getEconomy().depositPlayer(e.getPlayer(), amount);

                            if (!lastMinuteEarnings.containsKey(e.getPlayer().getUniqueId())) {
                                lastMinuteEarnings.put(e.getPlayer().getUniqueId(), (long) 0);
                            }

                            lastMinuteEarnings.put(e.getPlayer().getUniqueId(), lastMinuteEarnings.get(e.getPlayer().getUniqueId()) + amount);

                            e.getBlock().setType(Material.AIR);
                        }
                    }

                }).bindWith(core);
        /*Events.merge(BlockEnchantEvent.class, JackHammerTriggerEvent.class, ExplosionTriggerEvent.class)
                .handler(e -> {
                    if (disabledAutoSell.contains(e.getPlayer().getUniqueId())) {
                        e.getBlocksAffected().forEach(b -> b.getBlock().getDrops(e.getPlayer().getItemInHand()).forEach(i -> e.getPlayer().getInventory().addItem(i)));
                    } else {
                        ProtectedRegion reg = e.getMineRegion();

                        if (reg == null) {
                            return;
                        }

                        if (regionsAutoSell.containsKey(reg)) {
                            e.getBlocksAffected().forEach(b -> {
                                if (regionsAutoSell.get(reg).containsKey(b.getType())) {
                                    int amount = regionsAutoSell.get(reg).get(b.getType());

                                    econ.depositPlayer(e.getPlayer(), amount);

                                    if (!lastMinuteEarnings.containsKey(e.getPlayer())) {
                                        lastMinuteEarnings.put(e.getPlayer(), (long) 0);
                                    }

                                    lastMinuteEarnings.put(e.getPlayer(), lastMinuteEarnings.get(e.getPlayer()) + amount);
                                }
                            });
                        }
                    }
                }).bindWith(this);*/
    }

    public void disable() {

    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        toggleAutoSell(c.sender());
                    }
                }).registerAndBind(core, "autosell");
        Commands.create()
                .assertPlayer()
                .assertPermission("wildprison.sellprice")
                .handler(c -> {
                    if (c.args().size() == 1) {

                        if (c.sender().getItemInHand() == null) {
                            c.sender().sendMessage(Text.colorize("&cPlease hold some item!"));
                            return;
                        }

                        int price = c.arg(0).parseOrFail(Integer.class).intValue();
                        Material type = c.sender().getItemInHand().getType();
                        ProtectedRegion region = getFirstRegionAtLocation(c.sender().getLocation());

                        if (region == null) {
                            c.sender().sendMessage(Text.colorize("&cYou must be standing in a region!"));
                            return;
                        }

                        getConfig().set("regions." + region.getId() + ".world", c.sender().getWorld().getName());
                        getConfig().set("regions." + region.getId() + ".items." + type.name(), price);
                        getConfig().save();

                        HashMap<Material, Integer> prices;
                        if (regionsAutoSell.containsKey(region)) {
                            prices = regionsAutoSell.get(region);
                        } else {
                            prices = new HashMap<>();
                        }
                        prices.put(type, price);
                        regionsAutoSell.put(region, prices);

                        c.sender().sendMessage(Text.colorize(String.format("&aSuccessfuly set sell price of &e%s &ato &e$%d &ain region &e%s", type.name(), price, region.getId())));
                    }
                }).registerAndBind(core, "sellprice");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        ProtectedRegion region = this.getFirstRegionAtLocation(c.sender().getLocation());

                        if (region == null) {
                            c.sender().sendMessage(getMessage("not_in_region"));
                            return;
                        }


                        if (regionsAutoSell.containsKey(region)) {

                            long totalPrice = 0;

                            List<ItemStack> toRemove = new ArrayList<>();

                            for (Material m : regionsAutoSell.get(region).keySet()) {
                                for (ItemStack item : Arrays.stream(c.sender().getInventory().getContents()).filter(i -> i != null && i.getType() == m).collect(Collectors.toList())) {
                                    totalPrice += item.getAmount() * regionsAutoSell.get(region).get(m);
                                    toRemove.add(item);
                                }
                            }

                            toRemove.forEach(i -> c.sender().getInventory().removeItem(i));
                            totalPrice = (long) core.getMultipliers().getApi().getTotalToDeposit(c.sender(), totalPrice);
                            core.getEconomy().depositPlayer(c.sender(), totalPrice);
                            c.sender().sendMessage(getMessage("sell_all_complete").replace("%price%", String.format("%,d", totalPrice)));
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

    private ProtectedRegion getFirstRegionAtLocation(Location loc) {
        List<ProtectedRegion> regions = new ArrayList<>(WorldGuardPlugin.inst().getRegionContainer().createQuery().getApplicableRegions(loc).getRegions());
        return regions.size() == 0 ? null : regions.get(0);
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    public long getCurrentEarnings(Player player) {
        return lastMinuteEarnings.containsKey(player.getUniqueId()) ? lastMinuteEarnings.get(player.getUniqueId()) : 0;
    }

    public int getPriceForBrokenBlock(ProtectedRegion region, Block block) {
        return regionsAutoSell.containsKey(region) ? regionsAutoSell.get(region).containsKey(block.getType()) ? regionsAutoSell.get(region).get(block.getType()) : 0 : 0;
    }

    public boolean hasAutoSellEnabled(Player p) {
        return !disabledAutoSell.contains(p.getUniqueId());
    }
}
