package me.drawethree.wildprisoncore.autominer;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.autominer.gui.MainAutoMinerGui;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class WildPrisonAutoMiner {


    @Getter
    private static WildPrisonAutoMiner instance;
    @Getter
    private FileManager.Config config;

    private HashMap<String, String> messages;

    private HashMap<UUID, Long> autoMinerFuels;
    private HashMap<UUID, Integer> autoMinerLevels;
    private HashMap<UUID, Integer> autoMinerCommandLevels;

    private LinkedHashMap<Integer, AutoMinerFuelLevel> fuelLevels;
    private LinkedHashMap<Integer, AutoMinerCommandLevel> commandLevels;

    @Getter
    private AutoMinerFuelLevel lastLevel;
    @Getter
    private AutoMinerCommandLevel lastLevelCommand;

    @Getter
    private AutoMinerRegion region;
    @Getter
    private WildPrisonCore core;

    public WildPrisonAutoMiner(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("autominer.yml").copyDefaults(true).save();
    }

    public void enable() {
        instance = this;
        this.autoMinerFuels = new HashMap<>();
        this.autoMinerLevels = new HashMap<>();
        this.autoMinerCommandLevels = new HashMap<>();
        this.registerCommands();
        this.registerEvents();
        this.loadMessages();
        //this.removeExpiredAutoMiners();
        this.loadFuelLevels();
        this.loadCommandLevels();
        this.loadAutoMinerRegion();
        this.loadPlayersAutoMiner();
    }

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
    }

    private void loadCommandLevels() {
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
    }

    private void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.saveAutoMiner(e.getPlayer(), true)).bindWith(this.core);
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.loadAutoMiner(e.getPlayer())).bindWith(this.core);
    }

    private void loadPlayersAutoMiner() {
        Players.all().forEach(p -> loadAutoMiner(p));
    }

    private void removeExpiredAutoMiners() {
        Schedulers.async().run(() -> {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM " + MySQLDatabase.AUTOMINER_DB_NAME + " WHERE " + MySQLDatabase.AUTOMINER_FUEL_COLNAME + " <= 0")) {
                statement.execute();
                this.core.getLogger().info("Removed expired AutoMiners from database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadAutoMiner(Player p) {
        Schedulers.async().run(() -> {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.AUTOMINER_DB_NAME + " WHERE " + MySQLDatabase.AUTOMINER_UUID_COLNAME + "=?")) {
                statement.setString(1, p.getUniqueId().toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        long fuelLeft = set.getLong(MySQLDatabase.AUTOMINER_FUEL_COLNAME);
                        int level = set.getInt(MySQLDatabase.AUTOMINER_LEVEL_COLNAME);
                        int commandLevel = set.getInt(MySQLDatabase.AUTOMINER_COMMAND_LEVEL_COLNAME);
                        this.autoMinerFuels.put(p.getUniqueId(), fuelLeft);
                        this.autoMinerCommandLevels.put(p.getUniqueId(), commandLevel);
                        this.autoMinerLevels.put(p.getUniqueId(), level);
                        this.core.getLogger().info(String.format("Loaded %s's AutoMiner fuel (%d fuel, %d level, %d command level)", p.getName(), fuelLeft, level, commandLevel));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveAutoMiner(Player p, boolean async) {

        long fuelLeft = autoMinerFuels.getOrDefault(p.getUniqueId(), 0L);
        int fuelLevel = this.getPlayerLevel(p);
        int commandLevel = this.getPlayerCommandLevel(p);

        if (async) {
            Schedulers.async().run(() -> {
                try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + MySQLDatabase.AUTOMINER_DB_NAME + " VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.AUTOMINER_FUEL_COLNAME + "=?," + MySQLDatabase.AUTOMINER_LEVEL_COLNAME + "=?," + MySQLDatabase.AUTOMINER_COMMAND_LEVEL_COLNAME + "=?")) {
                    statement.setString(1, p.getUniqueId().toString());
                    statement.setLong(2, fuelLeft);
                    statement.setInt(3, fuelLevel);
                    statement.setInt(4, commandLevel);
                    statement.setLong(5, fuelLeft);
                    statement.setInt(6, fuelLevel);
                    statement.setInt(7, commandLevel);
                    statement.execute();
                    this.autoMinerFuels.remove(p.getUniqueId());
                    this.autoMinerLevels.remove(p.getUniqueId());
                    this.autoMinerCommandLevels.remove(p.getUniqueId());
                    this.core.getLogger().info(String.format("Saved %s's AutoMiner fuel and levels.", p.getName()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + MySQLDatabase.AUTOMINER_DB_NAME + " VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.AUTOMINER_FUEL_COLNAME + "=?," + MySQLDatabase.AUTOMINER_LEVEL_COLNAME + "=?," + MySQLDatabase.AUTOMINER_COMMAND_LEVEL_COLNAME + "=?")) {
                statement.setString(1, p.getUniqueId().toString());
                statement.setLong(2, fuelLeft);
                statement.setInt(3, fuelLevel);
                statement.setInt(4, commandLevel);
                statement.setLong(5, fuelLeft);
                statement.setInt(6, fuelLevel);
                statement.setInt(7, commandLevel);
                statement.execute();
                this.autoMinerFuels.remove(p.getUniqueId());
                this.autoMinerLevels.remove(p.getUniqueId());
                this.autoMinerCommandLevels.remove(p.getUniqueId());
                this.core.getLogger().info(String.format("Saved %s's AutoMiner fuel and levels.", p.getName()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPlayerCommandLevel(Player p) {
        return this.autoMinerCommandLevels.getOrDefault(p.getUniqueId(), 1);
    }

    private void loadAutoMinerRegion() {
        String worldName = getConfig().get().getString("auto-miner-region.world");
        String regionName = getConfig().get().getString("auto-miner-region.name");
        long moneyPerSec = getConfig().get().getLong("auto-miner-region.money");
        long tokensPerSec = getConfig().get().getLong("auto-miner-region.tokens");

        World world = Bukkit.getWorld(worldName);

        ProtectedRegion region = WorldGuardPlugin.inst().getRegionManager(world).getRegion(regionName);
        if (region == null) {
            core.getLogger().warning(String.format("There is no such region named %s in world %s!", regionName, world));
            return;
        }
        this.region = new AutoMinerRegion(this, world, region, moneyPerSec, tokensPerSec);
        core.getLogger().info("AutoMiner region loaded!");

    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    public void disable() {
        Players.all().forEach(p -> saveAutoMiner(p, false));
    }

    private void registerCommands() {
        /*Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("auto_miner_time").replace("%time%", this.getTimeLeft(c.sender())));
                    }
                }).registerAndBind(core, "miner", "autominer");*/

        // /adminautominer give {Player} {Amount of time}
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3 && c.rawArg(0).equalsIgnoreCase("give")) {
                        Player target = Players.getNullable(c.rawArg(1));
                        long fuel = c.arg(2).parseOrFail(Long.class).longValue();
                        givePlayerAutoMinerFuel(c.sender(), target, fuel);
                    }

                }).registerAndBind(core, "adminautominer", "aam");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        new MainAutoMinerGui(c.sender()).open();
                    }

                }).registerAndBind(core, "miner", "autominer");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("fuel_tank").replace("%fuel%", String.format("%,d", this.getPlayerFuel(c.sender()))));
                    }

                }).registerAndBind(core, "fueltank", "fuel");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        tryBuyNextLevel(c.sender());
                    }

                }).registerAndBind(core, "autominerlevelup");
    }

    public boolean tryBuyNextLevel(Player player) {

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
    }

    public boolean tryBuyNextCommandLevel(Player player) {

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

    }

    private boolean isAtMaxLevel(Player player) {
        return this.getPlayerLevel(player) == lastLevel.getLevel();
    }

    private boolean isAtMaxCommandLevel(Player player) {
        return this.getPlayerCommandLevel(player) == lastLevelCommand.getLevel();
    }

    private void givePlayerAutoMinerFuel(CommandSender sender, Player p, long fuel) {

        if (p == null || !p.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer is not online!"));
            return;
        }

        long currentFuel = autoMinerFuels.getOrDefault(p.getUniqueId(), 0L);
        currentFuel += fuel;

        autoMinerFuels.put(p.getUniqueId(), currentFuel);
        sender.sendMessage(messages.get("auto_miner_fuel_add").replace("%fuel%", String.valueOf(fuel)).replace("%player%", p.getName()));
    }

    public boolean hasAutoMinerFuel(Player p) {
        return autoMinerFuels.containsKey(p.getUniqueId()) && autoMinerFuels.get(p.getUniqueId()) > 0;
    }

    public void decrementFuel(Player p, long amount) {
        long newAmount = autoMinerFuels.get(p.getUniqueId()) - amount;
        autoMinerFuels.put(p.getUniqueId(), newAmount);
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    /*public String getTimeLeft(Player p) {

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
    }*/

    public long getPlayerFuel(Player p) {
        return this.autoMinerFuels.getOrDefault(p.getUniqueId(), 0L);
    }

    public int getPlayerLevel(Player p) {
        return this.autoMinerLevels.getOrDefault(p.getUniqueId(), 1);
    }

    public AutoMinerFuelLevel getAutoMinerFuelLevel(Player p) {

        long currentFuel = this.getPlayerFuel(p);
        int maxLevel = this.getPlayerLevel(p);

        for (int i = maxLevel; i > 0; i--) {
            AutoMinerFuelLevel level = this.fuelLevels.get(i);
            if (currentFuel >= level.getTreshold()) {
                return level;
            }
        }

        return this.fuelLevels.get(1);
    }

    public AutoMinerCommandLevel getAutoMinerCommandLevel(Player p) {

        int autoMinerLevel = this.getAutoMinerFuelLevel(p).getLevel();
        int maxLevel = this.getPlayerCommandLevel(p);

        if (maxLevel > autoMinerLevel) {
            maxLevel = autoMinerLevel;
        }

        return this.commandLevels.get(maxLevel);
    }

    public Collection<AutoMinerFuelLevel> getFuelLevels() {
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
    }


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
}
