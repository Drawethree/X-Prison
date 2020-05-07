package me.drawethree.wildprisoncore.autominer;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public final class WildPrisonAutoMiner {


    @Getter
    private FileManager.Config config;

    private HashMap<String, String> messages;
    private HashMap<UUID, Integer> autoMinerTimes;

    @Getter
    private AutoMinerRegion region;
    @Getter
    private WildPrisonCore core;

    public WildPrisonAutoMiner(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("autominer.yml").copyDefaults(true).save();
    }

    public void enable() {
        autoMinerTimes = new HashMap<>();
        this.registerCommands();
        this.registerEvents();
        this.loadMessages();
        this.loadAutoMinerRegion();
        this.loadPlayersAutoMiner();
    }

    private void registerEvents() {
        Events.subscribe(PlayerQuitEvent.class)
                .filter(e -> this.autoMinerTimes.containsKey(e.getPlayer().getUniqueId()))
                .handler(e -> {
                    this.saveAutoMiner(e.getPlayer(), true);
                }).bindWith(this.core);
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    this.loadAutoMiner(e.getPlayer());
                }).bindWith(this.core);
    }

    private void loadPlayersAutoMiner() {
        Players.all().forEach(p -> loadAutoMiner(p));
    }

    private void loadAutoMiner(Player p) {
        Schedulers.async().run(() -> {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.AUTOMINER_DB_NAME + " WHERE " + MySQLDatabase.AUTOMINER_UUID_COLNAME + "=?")) {
                statement.setString(1, p.getUniqueId().toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        long timeLeft = set.getLong(MySQLDatabase.AUTOMINER_TIMELEFT_COLNAME);
                        if (timeLeft > 0) {
                            this.autoMinerTimes.put(p.getUniqueId(), (int) timeLeft);
                            this.core.getLogger().info(String.format("Loaded %s's AutoMiner time (%d seconds)", p.getName(), timeLeft));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveAutoMiner(Player p, boolean async) {

        if (!autoMinerTimes.containsKey(p.getUniqueId())) {
            return;
        }

        int timeLeft = autoMinerTimes.get(p.getUniqueId());

        if (async) {
            Schedulers.async().run(() -> {
                try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + MySQLDatabase.AUTOMINER_DB_NAME + " VALUES (?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.AUTOMINER_TIMELEFT_COLNAME + "=?")) {
                    statement.setString(1, p.getUniqueId().toString());
                    statement.setLong(2, timeLeft);
                    statement.setLong(3, timeLeft);
                    statement.execute();
                    this.autoMinerTimes.remove(p.getUniqueId());
                    this.core.getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + MySQLDatabase.AUTOMINER_DB_NAME + " VALUES (?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.AUTOMINER_TIMELEFT_COLNAME + "=?")) {
                statement.setString(1, p.getUniqueId().toString());
                statement.setLong(2, timeLeft);
                statement.setLong(3, timeLeft);
                statement.execute();
                this.autoMinerTimes.remove(p.getUniqueId());
                this.core.getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadAutoMinerRegion() {
        String world = getConfig().get().getString("auto-miner-region.world");
        String regionName = getConfig().get().getString("auto-miner-region.name");
        int moneyPerSec = getConfig().get().getInt("auto-miner-region.money");
        int tokensPerSec = getConfig().get().getInt("auto-miner-region.tokens");

        ProtectedRegion region = WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
        if (region == null) {
            core.getLogger().warning(String.format("There is no such region named %s in world %s!", regionName, world));
            return;
        }
        this.region = new AutoMinerRegion(this, region, moneyPerSec, tokensPerSec);
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
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("auto_miner_time").replace("%time%", String.valueOf(autoMinerTimes.getOrDefault(c.sender().getUniqueId(), 0))));
                    }
                }).registerAndBind(core, "miner", "autominer");

        // /adminautominer give {Player} {Amount of time}
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3 && c.rawArg(0).equalsIgnoreCase("give")) {
                        Player target = Players.getNullable(c.rawArg(1));
                        int time = c.arg(2).parseOrFail(Integer.class).intValue();
                        givePlayerAutoMinerTime(c.sender(), target, time);
                    }

                }).registerAndBind(core, "adminautominer", "aam");
    }

    private void givePlayerAutoMinerTime(CommandSender sender, Player p, int seconds) {

        if (p == null || !p.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer is not online!"));
            return;
        }

        int currentSecs = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
        currentSecs += seconds;

        autoMinerTimes.put(p.getUniqueId(), currentSecs);
        sender.sendMessage(messages.get("auto_miner_time_add").replace("%time%", String.valueOf(seconds)).replace("%player%", p.getName()));
    }

    public boolean hasAutoMinerTime(Player p) {
        return autoMinerTimes.containsKey(p.getUniqueId()) && autoMinerTimes.get(p.getUniqueId()) > 0;
    }

    public void decrementTime(Player p) {
        autoMinerTimes.put(p.getUniqueId(), autoMinerTimes.get(p.getUniqueId()) - 1);
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }
}
