package me.drawethree.wildprisoncore.ranks.manager;

import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.drawethree.wildprisoncore.ranks.WildPrisonRankup;
import me.drawethree.wildprisoncore.ranks.rank.Prestige;
import me.drawethree.wildprisoncore.ranks.rank.Rank;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RankManager {

    private LinkedHashMap<Integer, Rank> ranksById;
    private LinkedHashMap<Integer, Prestige> prestigeById;

    private HashMap<UUID, Integer> onlinePlayersRanks = new HashMap<>();
    private HashMap<UUID, Integer> onlinePlayersPrestige = new HashMap<>();
    private WildPrisonRankup plugin;

    private Rank maxRank;
    private Prestige maxPrestige;

    private String SPACER_LINE;
    private String TOP_FORMAT_PRESTIGE;
    private boolean updating;
    private HashMap<UUID, Integer> top10Prestige;
    private Task task;

    public RankManager(WildPrisonRankup plugin) {
        this.plugin = plugin;
        this.SPACER_LINE = plugin.getMessage("top_spacer_line");
        this.TOP_FORMAT_PRESTIGE = plugin.getMessage("top_format_prestige");
        this.loadRanks();
        this.loadPrestiges();

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    loadPlayerRankAndPrestige(e.getPlayer());
                }).bindWith(plugin.getCore());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    savePlayerRankAndPrestige(e.getPlayer());
                }).bindWith(plugin.getCore());

        this.updateTop10();
    }

    public void saveAllDataSync() {
        for (UUID uuid : this.onlinePlayersRanks.keySet()) {
            this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.RANKS_DB_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=?," + MySQLDatabase.RANKS_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", onlinePlayersRanks.get(uuid), onlinePlayersPrestige.get(uuid), uuid.toString());
        }
        this.plugin.getCore().getLogger().info("Saved players ranks and prestiges!");
    }

    public void loadAllData() {
        for (Player p : Players.all()) {
            loadPlayerRankAndPrestige(p);
        }
    }

    private void savePlayerRankAndPrestige(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.RANKS_DB_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=?," + MySQLDatabase.RANKS_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", getPlayerRank(player).getId(), getPlayerPrestige(player).getId(), player.getUniqueId().toString());
            this.onlinePlayersPrestige.remove(player.getUniqueId());
            this.onlinePlayersRanks.remove(player.getUniqueId());
            this.plugin.getCore().getLogger().info("Saved " + player.getName() + "'s rank and prestige to database.");
        });
    }

    private void loadPlayerRankAndPrestige(Player player) {
        Schedulers.async().run(() -> {
            try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.RANKS_DB_NAME + " WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?")) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        this.onlinePlayersRanks.put(player.getUniqueId(), set.getInt(MySQLDatabase.RANKS_RANK_COLNAME));
                        this.onlinePlayersPrestige.put(player.getUniqueId(), set.getInt(MySQLDatabase.RANKS_PRESTIGE_COLNAME));
                        this.plugin.getCore().getLogger().info("Loaded " + player.getName() + "'s prestige and rank.");
                    } else {
                        this.plugin.getCore().getSqlDatabase().execute("INSERT IGNORE INTO " + MySQLDatabase.RANKS_DB_NAME + " VALUES(?,?,?)", player.getUniqueId().toString(), 1, 0);
                        this.plugin.getCore().getLogger().info("Added player " + player.getName() + " to database.");
                        this.onlinePlayersRanks.put(player.getUniqueId(), 1);
                        this.onlinePlayersPrestige.put(player.getUniqueId(), 0);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadRanks() {
        this.ranksById = new LinkedHashMap<>();
        for (String key : plugin.getConfig().get().getConfigurationSection("Ranks").getKeys(false)) {
            int id = Integer.parseInt(key);
            String prefix = Text.colorize(plugin.getConfig().get().getString("Ranks." + key + ".Prefix"));
            long cost = plugin.getConfig().get().getLong("Ranks." + key + ".Cost");
            List<String> commands = plugin.getConfig().get().getStringList("Ranks." + key + ".CMD");

            Rank rank = new Rank(id, cost, prefix, commands);
            this.ranksById.put(id, rank);
            this.maxRank = rank;
        }
        this.plugin.getCore().getLogger().info(String.format("Loaded %d ranks!", ranksById.keySet().size()));
    }

    private void loadPrestiges() {
        this.prestigeById = new LinkedHashMap<>();
        for (String key : this.plugin.getConfig().get().getConfigurationSection("Prestige").getKeys(false)) {
            int id = Integer.parseInt(key);
            String prefix = Text.colorize(this.plugin.getConfig().get().getString("Prestige." + key + ".Prefix"));
            long cost = this.plugin.getConfig().get().getLong("Prestige." + key + ".Cost");
            List<String> commands = this.plugin.getConfig().get().getStringList("Prestige." + key + ".CMD");
            Prestige p = new Prestige(id, cost, prefix, commands);
            this.prestigeById.put(id, p);
            this.maxPrestige = p;
        }
        this.plugin.getCore().getLogger().info(String.format("Loaded %d prestiges!", this.prestigeById.keySet().size()));
    }

    public Rank getNextRank(int id) {
        return this.ranksById.getOrDefault(id + 1, null);
    }

    public Rank getPreviousRank(int id) {
        return this.ranksById.getOrDefault(id - 1, null);
    }

    public Prestige getNextPrestige(int id) {
        return this.prestigeById.getOrDefault(id + 1, null);
    }

    public Prestige getPreviousPrestige(int id) {
        return this.prestigeById.getOrDefault(id - 1, null);
    }

    public Rank getPlayerRank(Player p) {
        return this.ranksById.getOrDefault(this.onlinePlayersRanks.get(p.getUniqueId()), this.ranksById.get(1));
    }

    public Prestige getPlayerPrestige(Player p) {
        return this.prestigeById.getOrDefault(this.onlinePlayersPrestige.get(p.getUniqueId()), this.prestigeById.get(0));
    }

    public boolean isMaxRank(Player p) {
        return this.getPlayerRank(p).getId() == this.maxRank.getId();
    }

    public boolean isMaxPrestige(Player p) {
        return this.getPlayerPrestige(p).getId() == this.maxPrestige.getId();
    }

    public boolean buyNextRank(Player p) {

        if (isMaxRank(p)) {
            p.sendMessage(this.plugin.getMessage("prestige_needed"));
            return false;
        }

        Rank currentRank = this.getPlayerRank(p);
        Rank toBuy = getNextRank(currentRank.getId());

        if (!this.plugin.getCore().getEconomy().has(p, toBuy.getCost())) {
            p.sendMessage(this.plugin.getMessage("not_enough_money").replace("%cost%", String.format("%,d", toBuy.getCost())));
            return false;
        }

        this.plugin.getCore().getEconomy().withdrawPlayer(p, toBuy.getCost());
        toBuy.runCommands(p);
        this.onlinePlayersRanks.put(p.getUniqueId(), toBuy.getId());
        p.sendMessage(this.plugin.getMessage("rank_up").replace("%Rank-1%", currentRank.getPrefix()).replace("%Rank-2%", toBuy.getPrefix()));
        return true;
    }

    public boolean buyNextPrestige(Player p) {

        if (!isMaxRank(p)) {
            p.sendMessage(this.plugin.getMessage("not_last_rank"));
            return false;
        }

        if (isMaxPrestige(p)) {
            p.sendMessage(this.plugin.getMessage("last_prestige"));
            return false;
        }

        Prestige currentPrestige = this.getPlayerPrestige(p);
        Prestige toBuy = getNextPrestige(currentPrestige.getId());

        if (!this.plugin.getCore().getEconomy().has(p, toBuy.getCost())) {
            p.sendMessage(this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,d", toBuy.getCost())));
            return false;
        }

        this.plugin.getCore().getEconomy().withdrawPlayer(p, toBuy.getCost());
        toBuy.runCommands(p);
        this.onlinePlayersPrestige.put(p.getUniqueId(), toBuy.getId());
        p.sendMessage(this.plugin.getMessage("prestige_up").replace("%Prestige-2%", toBuy.getPrefix()));
        return true;
    }

    public void sendPrestigeTop(CommandSender sender) {
        Schedulers.async().run(() -> {
            sender.sendMessage(Text.colorize(SPACER_LINE));
            if (this.updating) {
                sender.sendMessage(this.plugin.getMessage("top_updating"));
                sender.sendMessage(Text.colorize(SPACER_LINE));
                return;
            }
            for (int i = 0; i < 10; i++) {
                try {
                    UUID uuid = (UUID) top10Prestige.keySet().toArray()[i];
                    OfflinePlayer player = Players.getOfflineNullable(uuid);
                    String name;
                    if (player.getName() == null) {
                        name = "Unknown Player";
                    } else {
                        name = player.getName();
                    }
                    long prestige = top10Prestige.get(uuid);
                    sender.sendMessage(TOP_FORMAT_PRESTIGE.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%prestige%", String.format("%,d", prestige)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }
            sender.sendMessage(Text.colorize(SPACER_LINE));
        });
    }

    private void updateTop10() {
        this.updating = true;
        task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            this.saveAllDataSync();
            this.updatePrestigeTop();
            this.updating = false;
        }, 1, TimeUnit.MINUTES, 1, TimeUnit.HOURS);
    }

    public void stopUpdating() {
        this.plugin.getCore().getLogger().info("Stopping updating Top 10 - Prestige");
        task.close();
    }

    private void updatePrestigeTop() {
        top10Prestige = new LinkedHashMap<>();
        this.plugin.getCore().getLogger().info("Starting updating PrestigeTop");
        try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.RANKS_UUID_COLNAME + "," + MySQLDatabase.RANKS_PRESTIGE_COLNAME + " FROM " + MySQLDatabase.RANKS_DB_NAME + " ORDER BY " + MySQLDatabase.RANKS_PRESTIGE_COLNAME + " DESC LIMIT 10").executeQuery()) {
            while (set.next()) {
                top10Prestige.put(UUID.fromString(set.getString(MySQLDatabase.RANKS_UUID_COLNAME)), set.getInt(MySQLDatabase.RANKS_PRESTIGE_COLNAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.plugin.getCore().getLogger().info("PrestigeTop updated!");
    }
}
