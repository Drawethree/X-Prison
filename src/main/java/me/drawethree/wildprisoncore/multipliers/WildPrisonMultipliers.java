package me.drawethree.wildprisoncore.multipliers;

import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.drawethree.wildprisoncore.multipliers.api.WildPrisonMultipliersAPI;
import me.drawethree.wildprisoncore.multipliers.api.WildPrisonMultipliersAPIImpl;
import me.drawethree.wildprisoncore.multipliers.multiplier.GlobalMultiplier;
import me.drawethree.wildprisoncore.multipliers.multiplier.Multiplier;
import me.drawethree.wildprisoncore.multipliers.multiplier.PlayerMultiplier;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public final class WildPrisonMultipliers {

    private static GlobalMultiplier GLOBAL_MULTIPLIER;

    @Getter
    private static WildPrisonMultipliers instance;

    @Getter
    private FileManager.Config config;

    @Getter
    private WildPrisonMultipliersAPI api;

    private HashMap<UUID, Multiplier> rankMultipliers;
    private HashMap<UUID, PlayerMultiplier> personalMultipliers;

    private HashMap<String, String> messages;
    private LinkedHashMap<String, Double> permissionToMultiplier;

    @Getter
    private WildPrisonCore core;

    public WildPrisonMultipliers(WildPrisonCore wildPrisonCore) {
        instance = this;
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("multipliers.yml").copyDefaults(true).save();
        this.rankMultipliers = new HashMap<>();
        this.personalMultipliers = new HashMap<>();
        this.loadMessages();
        this.loadRankMultipliers();
    }


    private void loadRankMultipliers() {
        permissionToMultiplier = new LinkedHashMap<>();
        for (String rank : getConfig().get().getConfigurationSection("ranks").getKeys(false)) {
            String perm = "multiplier." + rank;
            double multiplier = getConfig().get().getDouble("ranks." + rank);
            permissionToMultiplier.put(perm, multiplier);
            core.getLogger().info("Loaded rank multiplier." + rank + " with multiplier " + multiplier);
        }
    }


    public void enable() {
        api = new WildPrisonMultipliersAPIImpl(this);
        this.registerCommands();
        this.registerEvents();
        this.removeExpiredMultipliers();
        this.loadGlobalMultiplier();
        this.loadOnlineMultipliers();
    }

    private void loadOnlineMultipliers() {
        Players.all().forEach(p -> {
            rankMultipliers.put(p.getUniqueId(), this.calculateRankMultiplier(p));
            this.loadPersonalMultiplier(p);
        });
    }

    private void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    rankMultipliers.put(e.getPlayer().getUniqueId(), this.calculateRankMultiplier(e.getPlayer()));
                    this.loadPersonalMultiplier(e.getPlayer());
                }).bindWith(core);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    rankMultipliers.remove(e.getPlayer().getUniqueId());
                    this.savePersonalMultiplier(e.getPlayer(), true);
                }).bindWith(core);
    }

    private void savePersonalMultiplier(Player player, boolean async) {

        if (!personalMultipliers.containsKey(player.getUniqueId())) {
            return;
        }

        PlayerMultiplier multiplier = personalMultipliers.get(player.getUniqueId());

        if (async) {
            Schedulers.async().run(() -> {
                try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + MySQLDatabase.MULTIPLIERS_DB_NAME + " VALUES(?,?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.MULTIPLIERS_VOTE_COLNAME + "=?, " + MySQLDatabase.MULTIPLIERS_VOTE_TIMELEFT_COLNAME + "=?")) {
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setDouble(2, multiplier.getMultiplier());
                    statement.setLong(3, multiplier.getEndTime());
                    statement.setDouble(4, multiplier.getMultiplier());
                    statement.setLong(5, multiplier.getEndTime());

                    statement.execute();
                    this.personalMultipliers.remove(player.getUniqueId());
                    this.core.getLogger().info(String.format("Saved multiplier of player %s", player.getName()));

                } catch (SQLException e) {
                    this.core.getLogger().warning("Could not save multiplier for player " + player.getName() + "!");
                    e.printStackTrace();
                }
            });
        } else {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + MySQLDatabase.MULTIPLIERS_DB_NAME + " VALUES(?,?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.MULTIPLIERS_VOTE_COLNAME + "=?, " + MySQLDatabase.MULTIPLIERS_VOTE_TIMELEFT_COLNAME + "=?")) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setDouble(2, multiplier.getMultiplier());
                statement.setLong(3, multiplier.getEndTime());
                statement.setDouble(4, multiplier.getMultiplier());
                statement.setLong(5, multiplier.getEndTime());

                statement.execute();
                this.personalMultipliers.remove(player.getUniqueId());
                this.core.getLogger().info(String.format("Saved multiplier of player %s", player.getName()));

            } catch (SQLException e) {
                this.core.getLogger().warning("Could not save multiplier for player " + player.getName() + "!");
                e.printStackTrace();
            }
        }
    }

    private void loadGlobalMultiplier() {
        double multi = this.config.get().getDouble("global-multiplier.multiplier");
        long timeLeft = this.config.get().getLong("global-multiplier.timeLeft");

        GLOBAL_MULTIPLIER = new GlobalMultiplier(0.0, 0);
        GLOBAL_MULTIPLIER.setMultiplier(multi);

        if (timeLeft > Time.nowMillis()) {
            GLOBAL_MULTIPLIER.setDuration(timeLeft);
        } else {
            GLOBAL_MULTIPLIER.setDuration((long) 0);
        }

        this.core.getLogger().info(String.format("Loaded Global Multiplier %.2fx", multi));

        /*Schedulers.async().run(() -> {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT " + MySQLDatabase.GLOBAL_MULTIPLIER_MULTIPLIER_COLNAME + ", " + MySQLDatabase.GLOBAL_MULTIPLIER_TIMELEFT_COLNAME + " FROM " + MySQLDatabase.GLOBAL_MULTIPLIER_DB_NAME)) {
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        double multiplier = set.getDouble(MySQLDatabase.GLOBAL_MULTIPLIER_MULTIPLIER_COLNAME);
                        long timeLeft = set.getLong(MySQLDatabase.GLOBAL_MULTIPLIER_TIMELEFT_COLNAME);
                        if (timeLeft > Time.nowMillis()) {
                            GLOBAL_MULTIPLIER.setMultiplier(multiplier);
                            GLOBAL_MULTIPLIER.setDuration((int) TimeUnit.MILLISECONDS.toMinutes(timeLeft - Time.nowMillis()));
                            this.core.getLogger().info(String.format("Loaded Global Multiplier %.2fx", multiplier));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });*/
    }

    private void saveGlobalMultiplier() {
        this.config.set("global-multiplier.multiplier", GLOBAL_MULTIPLIER.getMultiplier());
        this.config.set("global-multiplier.timeLeft", GLOBAL_MULTIPLIER.getEndTime());
        this.config.save();
        this.core.getLogger().info("Saved Global Multiplier into multipliers.yml");

        /*if (async) {
            Schedulers.async().run(() -> {
                try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE " + MySQLDatabase.GLOBAL_MULTIPLIER_DB_NAME + " SET " +  MySQLDatabase.GLOBAL_MULTIPLIER_TIMELEFT_COLNAME + "=?, " + MySQLDatabase.GLOBAL_MULTIPLIER_MULTIPLIER_COLNAME + "=?")) {
                    statement.setLong(1, GLOBAL_MULTIPLIER.getEndTime());
                    statement.setDouble(2, GLOBAL_MULTIPLIER.getMultiplier());
                    statement.execute();
                    this.core.getLogger().info("Saved Global Multiplier into database");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("UPDATE " + MySQLDatabase.GLOBAL_MULTIPLIER_DB_NAME + " SET " +  MySQLDatabase.GLOBAL_MULTIPLIER_TIMELEFT_COLNAME + "=?, " + MySQLDatabase.GLOBAL_MULTIPLIER_MULTIPLIER_COLNAME + "=?")) {
                statement.setLong(1, GLOBAL_MULTIPLIER.getEndTime());
                statement.setDouble(2, GLOBAL_MULTIPLIER.getMultiplier());
                statement.execute();
                this.core.getLogger().info("Saved Global Multiplier into database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/

    }

    private void loadPersonalMultiplier(Player player) {
        Schedulers.async().run(() -> {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.MULTIPLIERS_DB_NAME + " WHERE " + MySQLDatabase.MULTIPLIERS_UUID_COLNAME + "=?")) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        double multiplier = set.getDouble(MySQLDatabase.MULTIPLIERS_VOTE_COLNAME);
                        long endTime = set.getLong(MySQLDatabase.MULTIPLIERS_VOTE_TIMELEFT_COLNAME);
                        if (endTime > Time.nowMillis()) {
                            personalMultipliers.put(player.getUniqueId(), new PlayerMultiplier(player.getUniqueId(), multiplier, endTime));
                            this.core.getLogger().info(String.format("Loaded multiplier %.2fx for player %s", multiplier, player.getName()));
                        }
                    }
                }
            } catch (SQLException e) {
                this.core.getLogger().warning("Could not load multiplier for player " + player.getName() + "!");
                e.printStackTrace();
            }
        });
    }

    private void removeExpiredMultipliers() {
        Schedulers.async().run(() -> {
            try (Connection con = this.core.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM " + MySQLDatabase.MULTIPLIERS_DB_NAME + " WHERE " + MySQLDatabase.MULTIPLIERS_VOTE_TIMELEFT_COLNAME + " < " + Time.nowMillis())) {
                statement.execute();
                this.core.getLogger().info("Removed expired multipliers from database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    public void disable() {
        this.saveAllMultipliers();
    }

    private void saveAllMultipliers() {
        Players.all().forEach(p -> savePersonalMultiplier(p, false));
        this.saveGlobalMultiplier();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    private void registerCommands() {
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 2) {
                        double amount = c.arg(0).parseOrFail(Double.class).doubleValue();
                        int minutes = c.arg(1).parseOrFail(Integer.class).intValue();
                        setupGlobalMultiplier(c.sender(), minutes, amount);
                    }
                }).registerAndBind(core, "globalmultiplier", "gmulti");
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3) {
                        Player onlinePlayer = Players.getNullable(c.rawArg(0));
                        double amount = c.arg(1).parseOrFail(Double.class).doubleValue();
                        int minutes = c.arg(2).parseOrFail(Integer.class).intValue();
                        setupPersonalMultiplier(c.sender(), onlinePlayer, amount, minutes);
                    }
                }).registerAndBind(core, "personalmultiplier", "pmulti");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    c.sender().sendMessage(messages.get("global_multi").replace("%multiplier%", String.valueOf(GLOBAL_MULTIPLIER.getMultiplier())).replace("%duration%", GLOBAL_MULTIPLIER.getTimeLeft()));
                    c.sender().sendMessage(messages.get("rank_multi").replace("%multiplier%", String.valueOf(rankMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultPlayerMultiplier()).getMultiplier())));
                    c.sender().sendMessage(messages.get("vote_multi").replace("%multiplier%", String.valueOf(personalMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultPlayerMultiplier(c.sender().getUniqueId())).getMultiplier())).replace("%duration%", personalMultipliers.getOrDefault(c.sender().getUniqueId(), Multiplier.getDefaultPlayerMultiplier(c.sender().getUniqueId())).getTimeLeft()));
                }).registerAndBind(core, "multiplier", "multi");
    }

    private void setupPersonalMultiplier(CommandSender sender, Player onlinePlayer, double amount, int minutes) {
        if (amount > 1.5) {
            sender.sendMessage(Text.colorize("&cPersonal multiplier must be max 1.5!"));
            return;
        }
        if (onlinePlayer == null || !onlinePlayer.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer must be online!"));
            return;
        }

        if (personalMultipliers.containsKey(onlinePlayer.getUniqueId())) {
            PlayerMultiplier multiplier = personalMultipliers.get(onlinePlayer.getUniqueId());
            multiplier.addMultiplier(amount, 1.5);
            multiplier.addDuration(minutes);
        } else {
            personalMultipliers.put(onlinePlayer.getUniqueId(), new PlayerMultiplier(onlinePlayer.getUniqueId(), amount, minutes));
        }

        onlinePlayer.sendMessage(messages.get("personal_multi_apply").replace("%multiplier%", String.valueOf(amount)).replace("%minutes%", String.valueOf(minutes)));
        sender.sendMessage(Text.colorize(String.format("&aYou have set &e%s's &ePersonal Multiplier &ato &e%.2f &afor &e%d &aminutes.", onlinePlayer.getName(), amount, minutes)));
    }


    private void setupGlobalMultiplier(CommandSender sender, int time, double amount) {
        if (amount > 3.0) {
            sender.sendMessage(Text.colorize("&cGlobal multiplier must be max 3.0!"));
            return;
        }

        GLOBAL_MULTIPLIER.addMultiplier(amount, 3.0);
        GLOBAL_MULTIPLIER.addDuration(time);
        sender.sendMessage(Text.colorize(String.format("&aYou have set the &eGlobal Multiplier &ato &e%.2f &afor &e%d &aminutes.", amount, time)));
    }


    public double getGlobalMultiplier() {
        return GLOBAL_MULTIPLIER.getMultiplier();
    }

    public double getPersonalMultiplier(Player p) {
        return personalMultipliers.getOrDefault(p.getUniqueId(), Multiplier.getDefaultPlayerMultiplier(p.getUniqueId())).getMultiplier();
    }

    public double getRankMultiplier(Player p) {
        return rankMultipliers.getOrDefault(p.getUniqueId(), Multiplier.getDefaultPlayerMultiplier()).getMultiplier();
    }

    public void removePersonalMultiplier(UUID uuid) {
        personalMultipliers.remove(uuid);
    }


    private Multiplier calculateRankMultiplier(Player p) {
        PlayerMultiplier toReturn = new PlayerMultiplier(p.getUniqueId(), 0.0, -1);

        if (p.hasPermission("Store.Multiplier")) {
            toReturn.addMultiplier(2.0,3.5);
        }

        for (String perm : permissionToMultiplier.keySet()) {
            if (p.hasPermission(perm)) {
                toReturn.addMultiplier(permissionToMultiplier.get(perm), 3.5);
                break;
            }
        }

        return toReturn;
    }
}
