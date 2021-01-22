package me.drawethree.ultraprisoncore.autominer;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;

public final class UltraPrisonAutoMiner implements UltraPrisonModule {


    @Getter
    private static UltraPrisonAutoMiner instance;
    @Getter
    private FileManager.Config config;

    private HashMap<String, String> messages;

    private HashMap<UUID, Integer> autoMinerTimes;

    /*@Getter
    private AutoMinerFuelLevel lastLevel;
    @Getter
    private AutoMinerCommandLevel lastLevelCommand;*/

    @Getter
    private AutoMinerRegion region;
    @Getter
    private UltraPrisonCore core;
    private List<UUID> disabledAutoMiner;
    private boolean enabled;

    public UltraPrisonAutoMiner(UltraPrisonCore UltraPrisonCore) {
        this.core = UltraPrisonCore;
        this.config = UltraPrisonCore.getFileManager().getConfig("autominer.yml").copyDefaults(true).save();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.config = this.core.getFileManager().getConfig("autominer.yml");
        this.config.reload();
    }

    @Override
    public void enable() {
        this.enabled = true;
        instance = this;
        this.autoMinerTimes = new HashMap<>();
        this.disabledAutoMiner = new ArrayList<>();
        this.registerCommands();
        this.registerEvents();
        this.loadMessages();
        this.removeExpiredAutoMiners();
        //this.loadFuelLevels();
        //this.loadCommandLevels();
        this.loadAutoMinerRegion();
        this.loadPlayersAutoMiner();
    }

    /*
    private void loadFuelLevels() {
        this.fuelLevels = new LinkedHashMap<>();
        for (String key : this.config.get().getConfigurationSection("levels").getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                long cost = this.config.get().getLong("levels." + key + ".cost");
                long treshold = this.config.get().getLong("levels." + key + ".treshold");
                long fuelConsume = this.config.get().getLong("levels." + key + ".fuel_consume");
                double moneyPerSec = this.config.get().getDouble("levels." + key + ".money_per_sec");
                double tokensPerSec = this.config.get().getDouble("levels." + key + ".tokens_per_sec");
                ItemStack guiItem = ItemStackBuilder.of(Material.getMaterial(this.config.get().getString("levels." + key + ".gui_item.material"))).name(this.config.get().getString("levels." + key + ".gui_item.name")).lore(this.config.get().getStringList("levels." + key + ".gui_item.lore")).build();
                int guiItemSlot = this.config.get().getInt("levels." + key + ".gui_item.slot");
                AutoMinerFuelLevel autoMinerFuelLevel = new AutoMinerFuelLevel(level, cost, treshold, fuelConsume, moneyPerSec, tokensPerSec, guiItem, guiItemSlot);
                this.fuelLevels.put(level, autoMinerFuelLevel);
                this.lastLevel = autoMinerFuelLevel;
                this.core.getLogger().info("Loaded AutoMinerFuelLevel " + key + " !");
            } catch (Exception e) {
                this.core.getLogger().warning("Unable to load AutoMinerFuelLevel " + key + " !");
                e.printStackTrace();
                continue;
            }
        }
    }*/

    /*private void loadCommandLevels() {
        this.commandLevels = new LinkedHashMap<>();
        for (String key : this.config.get().getConfigurationSection("command_rewards").getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                long cost = this.config.get().getLong("command_rewards." + key + ".cost");

                ItemStack guiItem = ItemStackBuilder.of(Material.getMaterial(this.config.get().getString("command_rewards." + key + ".gui_item.material"))).name(this.config.get().getString("command_rewards." + key + ".gui_item.name")).lore(this.config.get().getStringList("command_rewards." + key + ".gui_item.lore")).build();
                int guiItemSlot = this.config.get().getInt("command_rewards." + key + ".gui_item.slot");

                List<String> commandsRaw = this.config.get().getStringList("command_rewards." + key + ".rewards");
                List<CommandReward> rewards = new ArrayList<>();

                for (String s : commandsRaw) {
                    String[] split = s.split(";");
                    String cmd = split[0];
                    double chance = Double.parseDouble(split[1]);
                    rewards.add(new CommandReward(cmd, chance));
                }

                AutoMinerCommandLevel autoMinerCommandLevel = new AutoMinerCommandLevel(level, cost, rewards, guiItem, guiItemSlot);
                this.commandLevels.put(level, autoMinerCommandLevel);
                this.lastLevelCommand = autoMinerCommandLevel;
                this.core.getLogger().info("Loaded AutoMinerCommandLevel " + key + " !");
            } catch (Exception e) {
                this.core.getLogger().warning("Unable to load AutoMinerCommandLevel " + key + " !");
                e.printStackTrace();
                continue;
            }
        }
    }*/

    private void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.saveAutoMiner(e.getPlayer(), true)).bindWith(this.core);
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.loadAutoMiner(e.getPlayer())).bindWith(this.core);
        /*Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getItem() != null && e.getItem().getType() == Material.DOUBLE_PLANT && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
                .handler(e -> {
                    if (e.getItem().hasItemMeta()) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        this.redeemFuel(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking());
                    }
                })
                .bindWith(this.core);

         */
    }

    private void loadPlayersAutoMiner() {
        Players.all().forEach(p -> loadAutoMiner(p));
    }

    private void removeExpiredAutoMiners() {
        Schedulers.async().run(() -> {
            this.core.getPluginDatabase().removeExpiredAutoMiners();
            this.core.getLogger().info("Removed expired AutoMiners from database");
        });
    }

    private void loadAutoMiner(Player p) {
        Schedulers.async().run(() -> {
            int timeLeft = this.core.getPluginDatabase().getPlayerAutoMinerTime(p);
            this.autoMinerTimes.put(p.getUniqueId(), timeLeft);
            this.core.getLogger().info(String.format("Loaded %s's AutoMiner Time.", p.getName()));
        });
    }

    private void saveAutoMiner(Player p, boolean async) {

        int timeLeft = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
        //int fuelLevel = this.getPlayerLevel(p);
        //int commandLevel = this.getPlayerCommandLevel(p);

        if (async) {
            Schedulers.async().run(() -> {
                this.core.getPluginDatabase().saveAutoMiner(p, timeLeft);
                this.autoMinerTimes.remove(p.getUniqueId());
                this.core.getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
            });
        } else {
            this.core.getPluginDatabase().saveAutoMiner(p, timeLeft);
            this.autoMinerTimes.remove(p.getUniqueId());
            this.core.getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
        }
    }

    /*public int getPlayerCommandLevel(Player p) {
        return this.autoMinerCommandLevels.getOrDefault(p.getUniqueId(), 1);
    }*/

    private void loadAutoMinerRegion() {
        String worldName = getConfig().get().getString("auto-miner-region.world");
        String regionName = getConfig().get().getString("auto-miner-region.name");

		double moneyPerSec = getConfig().get().getDouble("auto-miner-region.money");
		double tokensPerSec = getConfig().get().getDouble("auto-miner-region.tokens");

        World world = Bukkit.getWorld(worldName);

		if (world == null) {
			return;
		}

        Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(world,regionName);

        if (!optRegion.isPresent()) {
            core.getLogger().warning(String.format("There is no such region named %s in world %s!", regionName, world.getName()));
            return;
        }

        this.region = new AutoMinerRegion(this, world, optRegion.get(), moneyPerSec, tokensPerSec);
        core.getLogger().info("AutoMiner region loaded!");

    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    @Override
    public void disable() {
        Players.all().forEach(p -> saveAutoMiner(p, false));
        this.enabled = false;

    }

    @Override
    public String getName() {
        return "Auto Miner";
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("auto_miner_time").replace("%time%", this.getTimeLeft(c.sender())));
                    }
                }).registerAndBind(core, "miner", "autominer");
        Commands.create()
				.assertPermission("ultraprison.autominer.admin")
				.handler(c -> {
                    if (c.args().size() == 3 && c.rawArg(0).equalsIgnoreCase("give")) {
                        Player target = Players.getNullable(c.rawArg(1));
                        int time = c.arg(2).parseOrFail(Integer.class).intValue();
                        givePlayerAutoMinerTime(c.sender(), target, time);
                    }

                }).registerAndBind(core, "adminautominer", "aam");
        /*Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        new MainAutoMinerGui(c.sender()).open();
                    } else if (c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("toggle")) {
                        toggleAutoMiner(c.sender());
                    }

                }).registerAndBind(core, "miner", "autominer");*/
        /*Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("fuel_tank").replace("%fuel%", String.format("%,d", this.getPlayerFuel(c.sender()))));
                    } else {
                        FuelCommand subCommand = FuelCommand.getCommand(c.rawArg(0));
                        if (subCommand != null) {
                            subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                        }
                    }

                }).registerAndBind(core, "fueltank", "fuel");*/
        /*Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        tryBuyNextLevel(c.sender());
                    }

                }).registerAndBind(core, "autominerlevelup");*/
    }

    /*public boolean tryBuyNextLevel(Player player) {

        if (this.isAtMaxLevel(player)) {
            player.sendMessage(this.messages.get("last_level"));
            return false;
        }

        AutoMinerFuelLevel nextLevel = this.getNextLevel(player);

        if (this.core.getTokens().getTokensManager().getPlayerTokens(player) >= nextLevel.getCost()) {
            this.core.getTokens().getTokensManager().removeTokens(player, nextLevel.getCost(), null);
            this.autoMinerLevels.put(player.getUniqueId(), nextLevel.getLevel());
            player.sendMessage(this.messages.get("level_bought").replace("%level%", String.format("%,d", nextLevel.getLevel())));
            return true;
        } else {
            player.sendMessage(this.messages.get("not_enough_tokens").replace("%tokens%", String.format("%,d", nextLevel.getCost())));
            return false;
        }
    }*/

    /*public boolean tryBuyNextCommandLevel(Player player) {

        if (this.isAtMaxCommandLevel(player)) {
            player.sendMessage(this.messages.get("last_level"));
            return false;
        }

        AutoMinerCommandLevel nextLevel = this.getNextCommandLevel(player);

        if (this.core.getTokens().getTokensManager().getPlayerTokens(player) >= nextLevel.getCost()) {
            this.core.getTokens().getTokensManager().removeTokens(player, nextLevel.getCost(), null);
            this.autoMinerCommandLevels.put(player.getUniqueId(), nextLevel.getLevel());
            player.sendMessage(this.messages.get("level_bought").replace("%level%", String.format("%,d", nextLevel.getLevel())));
            return true;
        } else {
            player.sendMessage(this.messages.get("not_enough_tokens").replace("%tokens%", String.format("%,d", nextLevel.getCost())));
            return false;
        }

    }*/

    /*private boolean isAtMaxLevel(Player player) {
        return this.getPlayerLevel(player) == lastLevel.getLevel();
    }*/

    /*private boolean isAtMaxCommandLevel(Player player) {
        return this.getPlayerCommandLevel(player) == lastLevelCommand.getLevel();
    }*/

    private void givePlayerAutoMinerTime(CommandSender sender, Player p, long time) {

        if (p == null || !p.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer is not online!"));
            return;
        }

        int currentTime = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
        currentTime += time;

        autoMinerTimes.put(p.getUniqueId(), currentTime);
        sender.sendMessage(messages.get("auto_miner_time_add").replace("%time%", String.valueOf(time)).replace("%player%", p.getName()));
    }

    public boolean hasAutoMinerTime(Player p) {
        return autoMinerTimes.containsKey(p.getUniqueId()) && autoMinerTimes.get(p.getUniqueId()) > 0;
    }

    public void decrementTime(Player p) {
        int newAmount = autoMinerTimes.get(p.getUniqueId()) - 1;
        autoMinerTimes.put(p.getUniqueId(), newAmount);
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    public String getTimeLeft(Player p) {

        if (!autoMinerTimes.containsKey(p.getUniqueId())) {
            return "0s";
        }

        int secondsLeft = autoMinerTimes.get(p.getUniqueId());
        int timeLeft = secondsLeft;

        long days = timeLeft / (24 * 60 * 60);
        timeLeft -= days * (24 * 60 * 60);

        long hours = timeLeft / (60 * 60);
        timeLeft -= hours * (60 * 60);

        long minutes = timeLeft / (60);
        timeLeft -= minutes * (60);

        long seconds = timeLeft;

        timeLeft -= seconds;

        return new StringBuilder().append(days).append("d ").append(hours).append("h ").append(minutes).append("m ").append(seconds).append("s").toString();
    }

    /*public long getPlayerFuel(Player p) {
        return this.autoMinerFuels.getOrDefault(p.getUniqueId(), 0L);
    }*/

   /* public int getPlayerLevel(Player p) {
        return this.autoMinerLevels.getOrDefault(p.getUniqueId(), 1);
    }*/

    /*public AutoMinerFuelLevel getAutoMinerFuelLevel(Player p) {

        long currentFuel = this.getPlayerFuel(p);
        int maxLevel = this.getPlayerLevel(p);

        for (int i = maxLevel; i > 0; i--) {
            AutoMinerFuelLevel level = this.fuelLevels.get(i);
            if (currentFuel >= level.getTreshold()) {
                return level;
            }
        }

        return this.fuelLevels.get(1);
    }*/

    /*public AutoMinerCommandLevel getAutoMinerCommandLevel(Player p) {

        int autoMinerLevel = this.getAutoMinerFuelLevel(p).getLevel();
        int commandLevel = this.getPlayerCommandLevel(p);

        if (commandLevel > autoMinerLevel) {
            commandLevel = autoMinerLevel;
        }

        return this.commandLevels.get(commandLevel);
    }*/

    /*public Collection<AutoMinerFuelLevel> getFuelLevels() {
        return this.fuelLevels.values();
    }

    public Collection<AutoMinerCommandLevel> getCommandLevels() {
        return this.commandLevels.values();
    }

    public AutoMinerFuelLevel getNextLevel(Player p) {
        return this.fuelLevels.get(this.getPlayerLevel(p) + 1);
    }

    public AutoMinerCommandLevel getNextCommandLevel(Player p) {
        return this.commandLevels.get(this.getPlayerCommandLevel(p) + 1);
    }*/

    private void toggleAutoMiner(Player sender) {
        if (disabledAutoMiner.contains(sender.getUniqueId())) {
            sender.sendMessage(getMessage("autominer_enabled"));
            disabledAutoMiner.remove(sender.getUniqueId());
        } else {
            sender.sendMessage(getMessage("autominer_disabled"));
            disabledAutoMiner.add(sender.getUniqueId());
        }
    }

    public boolean hasAutominerOff(Player p) {
        return disabledAutoMiner.contains(p.getUniqueId());
    }

    /*public void payFuel(Player executor, long amount, Player target) {
        Schedulers.async().run(() -> {
            if (getPlayerFuel(executor) >= amount) {
                this.decrementFuel(executor, amount);
                this.addFuel(target, amount);
                executor.sendMessage(this.getMessage("fuel_send").replace("%player%", target.getName()).replace("%fuel%", String.format("%,d", amount)));
                if (target.isOnline()) {
                    target.sendMessage(this.getMessage("fuel_received").replace("%player%", executor.getName()).replace("%fuel%", String.format("%,d", amount)));
                }
            } else {
                executor.sendMessage(this.getMessage("not_enough_fuel"));
            }
        });
    }*/

    /*private void addFuel(Player p, long amount) {
        long newAmount = autoMinerFuels.get(p.getUniqueId()) + amount;
        autoMinerFuels.put(p.getUniqueId(), newAmount);
    }*/

    /*public void withdrawFuel(Player executor, long amount, int value) {
        Schedulers.async().run(() -> {
            long totalAmount = amount * value;

            if (this.getPlayerFuel(executor) < totalAmount) {
                executor.sendMessage(this.getMessage("not_enough_fuel"));
                return;
            }

            decrementFuel(executor, totalAmount);

            ItemStack item = createFuelItem(amount, value);
            Collection<ItemStack> notFit = executor.getInventory().addItem(item).values();

            if (!notFit.isEmpty()) {
                notFit.forEach(itemStack -> {
                    this.addFuel(executor, amount * item.getAmount());
                });
            }

            executor.sendMessage(this.getMessage("withdraw_successful").replace("%amount%", String.format("%,d,", amount)).replace("%value%", String.format("%,d", value)));
        });
    }*/

    /*public static ItemStack createFuelItem(long amount, int value) {
        return ItemStackBuilder.of(Material.EYE_OF_ENDER).amount(value).name("&e&l" + String.format("%,d", amount) + " FUEL").lore("&7Right-Click to Redeem").enchant(Enchantment.PROTECTION_ENVIRONMENTAL).flag(ItemFlag.HIDE_ENCHANTS).build();
    }

    public void redeemFuel(Player p, ItemStack item, boolean shiftClick) {
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        displayName = displayName.replace(" FUEL", "").replace(",", "");
        try {
            long tokenAmount = Long.parseLong(displayName);
            int itemAmount = item.getAmount();
            if (shiftClick) {
                p.setItemInHand(null);
                this.addFuel(p, tokenAmount * itemAmount);
                p.sendMessage(this.getMessage("fuel_redeem").replace("%fuel%", String.format("%,d", tokenAmount * itemAmount)));
            } else {
                this.addFuel(p, tokenAmount);
                if (item.getAmount() == 1) {
                    p.setItemInHand(null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                p.sendMessage(this.getMessage("fuel_redeem").replace("%fuel%", String.format("%,d", tokenAmount)));
            }
        } catch (Exception e) {
            //Not a fuel item
            p.sendMessage(this.getMessage("not_fuel_item"));
        }
        */
    }

/*
    @AllArgsConstructor
    @Getter
    public class AutoMinerFuelLevel {
        private int level;
        private long cost;
        private long treshold;
        private long fuelConsume;
        private double moneyPerSec;
        private double tokensPerSec;
        private ItemStack guiItem;
        private int guiItemSlot;
    }

    @AllArgsConstructor
    @Getter
    public class AutoMinerCommandLevel {
        private int level;
        private long cost;
        private List<CommandReward> commandRewards;
        private ItemStack guiItem;
        private int guiItemSlot;

        public void giveRewards(Player p) {
            Schedulers.sync().run(() -> {
                for (CommandReward reward : this.commandRewards) {
                    if (ThreadLocalRandom.current().nextDouble(100.0) <= reward.getChance()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.getCommand().replace("%player%", p.getName()));
                    }
                }
            });
        }
    }

    @AllArgsConstructor
    @Getter
    public class CommandReward {
        private String command;
        private double chance;
    }
}*/
