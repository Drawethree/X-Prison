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
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RankManager {

    private LinkedHashMap<Integer, Rank> ranksById;
    private LinkedHashMap<Integer, Prestige> prestigeById;
    private LinkedHashMap<Long, List<String>> prestigeRewards;

    private int minPrestigeLevel;
    private int maxPrestigeLevel;
    private double prestigeIncreaseCost;
    private List<String> prestigeCommands;

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
        this.loadPrestigeRewards();

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

    private void loadPrestigeRewards() {
        this.prestigeRewards = new LinkedHashMap<>();
        this.prestigeRewards.put(1000L, Arrays.asList("essentials.warps.p1k", "essentials.warps.p1000"));
        this.prestigeRewards.put(2500L, Arrays.asList("essentials.warps.p2.5k", "essentials.warps.p2500"));
        this.prestigeRewards.put(5000L, Arrays.asList("essentials.warps.p5k", "essentials.warps.p5000"));
        this.prestigeRewards.put(15000L, Arrays.asList("essentials.warps.p15k", "essentials.warps.p15000"));
        this.prestigeRewards.put(25000L, Arrays.asList("essentials.warps.p25k", "essentials.warps.p25000"));
        this.prestigeRewards.put(40000L, Arrays.asList("essentials.warps.p40k", "essentials.warps.p40000"));
        this.prestigeRewards.put(50000L, Arrays.asList("essentials.warps.p50k", "essentials.warps.p50000"));
        this.prestigeRewards.put(75000L, Arrays.asList("essentials.warps.p75k", "essentials.warps.p75000"));
        this.prestigeRewards.put(100000L, Arrays.asList("essentials.warps.p100k", "essentials.warps.p100000"));
        this.prestigeRewards.put(150000L, Arrays.asList("essentials.warps.p150k", "essentials.warps.p150000"));
        this.prestigeRewards.put(200000L, Arrays.asList("essentials.warps.p200k", "essentials.warps.p200000"));
        this.prestigeRewards.put(350000L, Arrays.asList("essentials.warps.p350k", "essentials.warps.p350000"));
        this.prestigeRewards.put(500000L, Arrays.asList("essentials.warps.p500k", "essentials.warps.p500000"));
        this.prestigeRewards.put(750000L, Arrays.asList("essentials.warps.p750k", "essentials.warps.p750000"));
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
            this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.RANKS_DB_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=?," + MySQLDatabase.RANKS_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", getPlayerRank(player).getId(), getPlayerPrestige(player), player.getUniqueId().toString());
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
        /*this.prestigeById = new LinkedHashMap<>();
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
         */

        this.prestigeIncreaseCost = this.plugin.getConfig().get().getDouble("Prestiges.increase-per-prestige");
        this.minPrestigeLevel = this.plugin.getConfig().get().getInt("Prestiges.min");
        this.maxPrestigeLevel = this.plugin.getConfig().get().getInt("Prestiges.max");
        this.prestigeCommands = this.plugin.getConfig().get().getStringList("Prestiges.commands");
    }

    public Rank getNextRank(int id) {
        return this.ranksById.getOrDefault(id + 1, null);
    }

    public Rank getPreviousRank(int id) {
        return this.ranksById.getOrDefault(id - 1, null);
    }

    public int getNextPrestige(int id) {
        return id + 1;
    }

    public Prestige getPreviousPrestige(int id) {
        return this.prestigeById.getOrDefault(id - 1, null);
    }

    public Rank getPlayerRank(Player p) {
        return this.ranksById.getOrDefault(this.onlinePlayersRanks.get(p.getUniqueId()), this.ranksById.get(1));
    }

    public int getPlayerPrestige(Player p) {
        return this.onlinePlayersPrestige.getOrDefault(p.getUniqueId(), this.minPrestigeLevel);
    }

    public boolean isMaxRank(Player p) {
        return this.getPlayerRank(p).getId() == this.maxRank.getId();
    }

    public boolean isMaxPrestige(Player p) {
        return this.getPlayerPrestige(p) == this.maxPrestigeLevel;
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

        int currentPrestige = this.getPlayerPrestige(p);
        int toBuy = getNextPrestige(currentPrestige);

        if (!this.plugin.getCore().getEconomy().has(p, toBuy * prestigeIncreaseCost)) {
            p.sendMessage(this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", (double) toBuy * prestigeIncreaseCost)));
            return false;
        }

        this.plugin.getCore().getEconomy().withdrawPlayer(p, toBuy * prestigeIncreaseCost);

            /*
            for (String s : this.prestigeCommands) {
                Schedulers.sync().run(() -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()));
                });
            }
             */

        this.onlinePlayersPrestige.put(p.getUniqueId(), toBuy);

        if (this.prestigeRewards.get(toBuy) != null) {
            for (String s : this.prestigeRewards.get(toBuy)) {
				/*this.plugin.getCore().getLuckPerms().getUserManager().getUser(p.getUniqueId()).data().add(Node.builder(s).build());
                this.plugin.getCore().getLuckPerms().getUserManager().saveUser(this.plugin.getCore().getLuckPerms().getUserManager().getUser(p.getUniqueId()));*/
                Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + p.getName() + " perm set " + s));
            }
        }

        p.sendMessage(this.plugin.getMessage("prestige_up").replace("%Prestige-2%", this.plugin.getApi().getPrestigePrefix(toBuy)));

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

    public boolean buyMaxPrestige(Player p) {

        if (!isMaxRank(p)) {
            p.sendMessage(this.plugin.getMessage("not_last_rank"));
            return false;
        }

        if (isMaxPrestige(p)) {
            p.sendMessage(this.plugin.getMessage("last_prestige"));
            return false;
        }

        double totalMoney = 0;
        int boughtTotal = 0;

        int startPrestige = this.getPlayerPrestige(p);
        int currentPrestige = this.getPlayerPrestige(p);

        if (!this.plugin.getCore().getEconomy().has(p, (startPrestige + 1) * prestigeIncreaseCost)) {
            p.sendMessage(this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", (double) (startPrestige + 1) * prestigeIncreaseCost)));
            return false;
        }

        while (!isMaxPrestige(p) && this.plugin.getCore().getEconomy().has(p, totalMoney + (currentPrestige + boughtTotal + 1) * prestigeIncreaseCost)) {

            totalMoney += (currentPrestige + boughtTotal + 1) * prestigeIncreaseCost;
            boughtTotal++;

            if (boughtTotal % 50 == 0) {
                this.plugin.getCore().getEconomy().withdrawPlayer(p, totalMoney);
                totalMoney = 0;
                this.onlinePlayersPrestige.put(p.getUniqueId(), this.onlinePlayersPrestige.get(p.getUniqueId()) + boughtTotal);
                boughtTotal = 0;
                currentPrestige = this.onlinePlayersPrestige.get(p.getUniqueId());
            }
        }

        this.plugin.getCore().getEconomy().withdrawPlayer(p, totalMoney);

        this.onlinePlayersPrestige.put(p.getUniqueId(), this.onlinePlayersPrestige.get(p.getUniqueId()) + boughtTotal);

        for (long l : this.prestigeRewards.keySet()) {
            if (this.onlinePlayersPrestige.get(p.getUniqueId()) >= l) {
                for (String s : this.prestigeRewards.get(l)) {
                   /* this.plugin.getCore().getLuckPerms().getUserManager().getUser(p.getUniqueId()).data().add(Node.builder(s).build());
                    this.plugin.getCore().getLuckPerms().getUserManager().saveUser(this.plugin.getCore().getLuckPerms().getUserManager().getUser(p.getUniqueId()));*/
                    Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + p.getName() + " perm set " + s));
                }
            }
        }

        if (startPrestige < this.onlinePlayersPrestige.get(p.getUniqueId())) {
            p.sendMessage(Text.colorize(String.format("&e&lPRESTIGE &8» &7Congratulations, you've max prestiged from &cP%,d &7to &cP%,d&7.", startPrestige, this.onlinePlayersPrestige.get(p.getUniqueId()))));
        }

        return true;
    }

    public void givePrestige(Player player, int levels) {

        if (!isMaxRank(player) || isMaxPrestige(player)) {
            return;
        }

        int currentPrestige = this.getPlayerPrestige(player);

        if (currentPrestige + levels > this.maxPrestigeLevel) {
            this.onlinePlayersPrestige.put(player.getUniqueId(), this.maxPrestigeLevel);
        } else {
            this.onlinePlayersPrestige.put(player.getUniqueId(), this.onlinePlayersPrestige.get(player.getUniqueId()) + levels);
        }

        for (long l : this.prestigeRewards.keySet()) {
            if (this.onlinePlayersPrestige.get(player.getUniqueId()) >= l) {
                for (String s : this.prestigeRewards.get(l)) {
                    /*this.plugin.getCore().getLuckPerms().getUserManager().getUser(player.getUniqueId()).data().add(Node.builder(s).build());
                    this.plugin.getCore().getLuckPerms().getUserManager().saveUser(this.plugin.getCore().getLuckPerms().getUserManager().getUser(player.getUniqueId()));*/
                    Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " perm set " + s));
                }
            }
        }

        player.sendMessage(Text.colorize("&e&lPRESTIGE FINDER &8» &7You've just found a &fX" + levels + " Prestige Level &7while mining."));
    }
}
