package me.drawethree.wildprisonrankup.manager;

import me.drawethree.wildprisonrankup.WildPrisonRankup;
import me.drawethree.wildprisonrankup.database.MySQLDatabase;
import me.drawethree.wildprisonrankup.rank.Prestige;
import me.drawethree.wildprisonrankup.rank.Rank;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class RankManager {

    private LinkedHashMap<Integer, Rank> ranksById;
    private LinkedHashMap<Integer, Prestige> prestigeById;

    private HashMap<UUID, Integer> onlinePlayersRanks = new HashMap<>();
    private HashMap<UUID, Integer> onlinePlayersPrestige = new HashMap<>();
    private WildPrisonRankup plugin;

    private Rank maxRank;
    private Prestige maxPrestige;

    public RankManager(WildPrisonRankup plugin) {
        this.plugin = plugin;
        this.loadRanks();
        this.loadPrestiges();

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    loadPlayerRankAndPrestige(e.getPlayer());
                }).bindWith(plugin);
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    savePlayerRankAndPrestige(e.getPlayer());
                }).bindWith(plugin);
    }

    public void saveAllDataSync() {
        for (UUID uuid : onlinePlayersRanks.keySet()) {
            plugin.getSqlDatabase().execute("UPDATE " + MySQLDatabase.RANKS_DB_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=?," + MySQLDatabase.RANKS_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", onlinePlayersRanks.get(uuid), onlinePlayersPrestige.get(uuid), uuid.toString());
        }
        plugin.getLogger().info("Saved players ranks and prestiges!");
        onlinePlayersRanks.clear();
        onlinePlayersPrestige.clear();
    }

    public void loadAllData() {
        for (Player p : Players.all()) {
            loadPlayerRankAndPrestige(p);
        }
    }

    private void savePlayerRankAndPrestige(Player player) {
        Schedulers.async().run(()-> {
            plugin.getSqlDatabase().execute("UPDATE " + MySQLDatabase.RANKS_DB_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=?," + MySQLDatabase.RANKS_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", getPlayerRank(player).getId(), getPlayerPrestige(player).getId(), player.getUniqueId().toString());
            onlinePlayersPrestige.remove(player.getUniqueId());
            onlinePlayersRanks.remove(player.getUniqueId());
            plugin.getLogger().info("Saved " + player.getName() + "'s rank and prestige to database.");
        });
    }

    private void loadPlayerRankAndPrestige(Player player) {
        Schedulers.async().run(() -> {
            try (Connection con = plugin.getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.RANKS_DB_NAME + " WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?")) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        onlinePlayersRanks.put(player.getUniqueId(), set.getInt(MySQLDatabase.RANKS_RANK_COLNAME));
                        onlinePlayersPrestige.put(player.getUniqueId(), set.getInt(MySQLDatabase.RANKS_PRESTIGE_COLNAME));
                        plugin.getLogger().info("Loaded " + player.getName() + "'s prestige and rank.");
                    } else {
                        plugin.getSqlDatabase().execute("INSERT IGNORE INTO " + MySQLDatabase.RANKS_DB_NAME + " VALUES(?,?,?)", player.getUniqueId().toString(), 1, 0);
                        plugin.getLogger().info("Added player " + player.getName() + " to database.");
                        onlinePlayersRanks.put(player.getUniqueId(), 1);
                        onlinePlayersPrestige.put(player.getUniqueId(), 0);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadRanks() {
        ranksById = new LinkedHashMap<>();
        for (String key : plugin.getConfig().getConfigurationSection("Ranks").getKeys(false)) {
            int id = Integer.parseInt(key);
            String prefix = Text.colorize(plugin.getConfig().getString("Ranks." + key + ".Prefix"));
            long cost = plugin.getConfig().getLong("Ranks." + key + ".Cost");
            List<String> commands = plugin.getConfig().getStringList("Ranks." + key + ".CMD");

            Rank rank = new Rank(id, cost, prefix, commands);
            ranksById.put(id, rank);
            maxRank = rank;
        }
        plugin.getLogger().info(String.format("Loaded %d ranks!", ranksById.keySet().size()));
    }

    private void loadPrestiges() {
        prestigeById = new LinkedHashMap<>();
        for (String key : plugin.getConfig().getConfigurationSection("Prestige").getKeys(false)) {
            int id = Integer.parseInt(key);
            String prefix = Text.colorize(plugin.getConfig().getString("Prestige." + key + ".Prefix"));
            long cost = plugin.getConfig().getLong("Prestige." + key + ".Cost");
            List<String> commands = plugin.getConfig().getStringList("Prestige." + key + ".CMD");
            Prestige p = new Prestige(id, cost, prefix, commands);
            prestigeById.put(id, p);
            maxPrestige = p;
        }
        plugin.getLogger().info(String.format("Loaded %d prestiges!", prestigeById.keySet().size()));
    }

    public Rank getNextRank(int id) {
        return ranksById.getOrDefault(id + 1, null);
    }

    public Rank getPreviousRank(int id) {
        return ranksById.getOrDefault(id - 1, null);
    }

    public Prestige getNextPrestige(int id) {
        return prestigeById.getOrDefault(id + 1, null);
    }

    public Prestige getPreviousPrestige(int id) {
        return prestigeById.getOrDefault(id - 1, null);
    }

    public Rank getPlayerRank(Player p) {
        return this.ranksById.getOrDefault(this.onlinePlayersRanks.get(p.getUniqueId()), this.ranksById.get(1));
    }

    public Prestige getPlayerPrestige(Player p) {
        return this.prestigeById.getOrDefault(this.onlinePlayersPrestige.get(p.getUniqueId()), this.prestigeById.get(0));
    }

    public boolean isMaxRank(Player p) {
        return getPlayerRank(p).getId() == maxRank.getId();
    }

    public boolean isMaxPrestige(Player p) {
        return getPlayerPrestige(p).getId() == maxPrestige.getId();
    }

    public boolean buyNextRank(Player p) {

        if (isMaxRank(p)) {
            p.sendMessage(plugin.getMessage("prestige_needed"));
            return false;
        }

        Rank currentRank = this.getPlayerRank(p);
        Rank toBuy = getNextRank(currentRank.getId());

        if (!plugin.getEconomy().has(p, toBuy.getCost())) {
            p.sendMessage(plugin.getMessage("not_enough_money").replace("%cost%", String.format("%,d", toBuy.getCost())));
            return false;
        }

        plugin.getEconomy().withdrawPlayer(p, toBuy.getCost());
        toBuy.runCommands(p);
        this.onlinePlayersRanks.put(p.getUniqueId(), toBuy.getId());
        p.sendMessage(plugin.getMessage("rank_up").replace("%Rank-1%", currentRank.getPrefix()).replace("%Rank-2%", toBuy.getPrefix()));
        return true;
    }

    public boolean buyNextPrestige(Player p) {

        if (!isMaxRank(p)) {
            p.sendMessage(plugin.getMessage("not_last_rank"));
            return false;
        }

        if (isMaxPrestige(p)) {
            p.sendMessage(plugin.getMessage("last_prestige"));
            return false;
        }

        Prestige currentPrestige = this.getPlayerPrestige(p);
        Prestige toBuy = getNextPrestige(currentPrestige.getId());

        if (!plugin.getEconomy().has(p, toBuy.getCost())) {
            p.sendMessage(plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,d", toBuy.getCost())));
            return false;
        }

        plugin.getEconomy().withdrawPlayer(p, toBuy.getCost());
        toBuy.runCommands(p);
        this.onlinePlayersPrestige.put(p.getUniqueId(), toBuy.getId());
        p.sendMessage(plugin.getMessage("prestige_up").replace("%Prestige-2%", toBuy.getPrefix()));
        return true;
    }

    public void sendPrestigeTop(CommandSender sender) {
        //TODO: Send top prestige players
        sender.sendMessage("§cTo be done...");

    }
}
