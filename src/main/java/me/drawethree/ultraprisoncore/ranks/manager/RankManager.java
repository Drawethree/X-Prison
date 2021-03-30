package me.drawethree.ultraprisoncore.ranks.manager;

import me.drawethree.ultraprisoncore.ranks.UltraPrisonRankup;
import me.drawethree.ultraprisoncore.ranks.rank.Prestige;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RankManager {

    private LinkedHashMap<Integer, Rank> ranksById;
    private LinkedHashMap<Long, Prestige> prestigeById;

    private HashMap<UUID, Integer> onlinePlayersRanks = new HashMap<>();
    private HashMap<UUID, Long> onlinePlayersPrestige = new HashMap<>();

    private UltraPrisonRankup plugin;

    private Rank maxRank;
    private Prestige maxPrestige;

    private String SPACER_LINE_BOTTOM;
    private String SPACER_LINE;
    private String TOP_FORMAT_PRESTIGE;
    private boolean updating;
    private boolean unlimitedPrestiges;
    private double unlimitedPrestigeCost;
    private boolean increaseCostEnabled;
    private double increaseCostBy;
    private boolean resetRankAfterPrestige;
    private String unlimitedPrestigePrefix;
    private long unlimitedPrestigeMax;
    private Map<Long, List<String>> unlimitedPrestigesRewards;
    private LinkedHashMap<UUID, Integer> top10Prestige;
    private Task task;
	private int prestigeTopUpdateInterval;
    private boolean unlimitedPrestigesRewardPerPrestigeEnabled;
    private List<String> unlimitedPrestigesRewardPerPrestige;

    public RankManager(UltraPrisonRankup plugin) {
        this.plugin = plugin;
        this.reload();

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    this.addIntoTable(e.getPlayer());
                    loadPlayerRankAndPrestige(e.getPlayer());
                }).bindWith(plugin.getCore());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    savePlayerRankAndPrestige(e.getPlayer());
                }).bindWith(plugin.getCore());

        this.updateTop10();
    }

    private void addIntoTable(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getCore().getPluginDatabase().addIntoRanksAndPrestiges(player);
        });
    }

    public void saveAllDataSync() {
        for (UUID uuid : this.onlinePlayersRanks.keySet()) {
            this.plugin.getCore().getPluginDatabase().updateRankAndPrestige(Players.getOfflineNullable(uuid), onlinePlayersRanks.get(uuid), onlinePlayersPrestige.get(uuid));
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
            this.plugin.getCore().getPluginDatabase().updateRankAndPrestige(player, this.getPlayerRank(player).getId(), this.getPlayerPrestige(player).getId());
            this.onlinePlayersPrestige.remove(player.getUniqueId());
            this.onlinePlayersRanks.remove(player.getUniqueId());
            this.plugin.getCore().getLogger().info("Saved " + player.getName() + "'s rank and prestige to database.");
        });
    }

    private void loadPlayerRankAndPrestige(Player player) {
        Schedulers.async().run(() -> {

            int rank = this.plugin.getCore().getPluginDatabase().getPlayerRank(player);
            long prestige = this.plugin.getCore().getPluginDatabase().getPlayerPrestige(player);

            this.onlinePlayersRanks.put(player.getUniqueId(), rank);
            this.onlinePlayersPrestige.put(player.getUniqueId(), prestige);
            this.plugin.getCore().getLogger().info("Loaded " + player.getName() + "'s prestige and rank.");

        });
    }

    public void reload() {
        this.SPACER_LINE = plugin.getMessage("top_spacer_line");
        this.SPACER_LINE_BOTTOM = plugin.getMessage("top_spacer_line_bottom");
        this.TOP_FORMAT_PRESTIGE = plugin.getMessage("top_format_prestige");

        this.unlimitedPrestiges = plugin.getConfig().get().getBoolean("unlimited_prestiges.enabled");
        this.unlimitedPrestigeCost = plugin.getConfig().get().getDouble("unlimited_prestiges.prestige_cost");
        this.unlimitedPrestigePrefix = Text.colorize(plugin.getConfig().get().getString("unlimited_prestiges.prefix"));
        this.unlimitedPrestigeMax = plugin.getConfig().get().getLong("unlimited_prestiges.max_prestige");

        this.increaseCostEnabled = plugin.getConfig().get().getBoolean("unlimited_prestiges.increase_cost.enabled");

        this.increaseCostBy = plugin.getConfig().get().getDouble("unlimited_prestiges.increase_cost.increase_cost_by");
        this.unlimitedPrestigesRewardPerPrestigeEnabled = plugin.getConfig().get().getBoolean("unlimited_prestiges.rewards-per-prestige.enabled");
        if (this.unlimitedPrestigesRewardPerPrestigeEnabled) {
            this.unlimitedPrestigesRewardPerPrestige = plugin.getConfig().get().getStringList("unlimited_prestiges.rewards-per-prestige.rewards");
        }

        this.resetRankAfterPrestige = plugin.getConfig().get().getBoolean("reset_rank_after_prestige");

		this.prestigeTopUpdateInterval = plugin.getConfig().get().getInt("prestige_top_update_interval");

        this.loadUnlimitedPrestigesRewards();

        this.loadRanks();
        this.loadPrestiges();
    }

    private void loadUnlimitedPrestigesRewards() {
        this.unlimitedPrestigesRewards = new LinkedHashMap<>();

        ConfigurationSection section = plugin.getConfig().get().getConfigurationSection("unlimited_prestiges.rewards");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                long id = Long.parseLong(key);

                List<String> rewards = section.getStringList(key);

                if (rewards != null && !rewards.isEmpty()) {
                    this.unlimitedPrestigesRewards.put(id, rewards);
                }
            } catch (Exception e) {
                continue;
            }
        }

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

        if (this.unlimitedPrestiges) {
            this.plugin.getCore().getLogger().info(String.format("Loaded %,d prestiges.", this.unlimitedPrestigeMax));
        } else {
            for (String key : this.plugin.getConfig().get().getConfigurationSection("Prestige").getKeys(false)) {
                long id = Long.parseLong(key);
                String prefix = Text.colorize(this.plugin.getConfig().get().getString("Prestige." + key + ".Prefix"));
                long cost = this.plugin.getConfig().get().getLong("Prestige." + key + ".Cost");
                List<String> commands = this.plugin.getConfig().get().getStringList("Prestige." + key + ".CMD");
                Prestige p = new Prestige(id, cost, prefix, commands);
                this.prestigeById.put(id, p);
                this.maxPrestige = p;
            }
            this.plugin.getCore().getLogger().info(String.format("Loaded %,d prestiges!", this.prestigeById.keySet().size()));
        }
    }

    public Rank getNextRank(int id) {
        return this.ranksById.getOrDefault(id + 1, null);
    }

    public Rank getPreviousRank(long id) {
        return this.ranksById.getOrDefault(id - 1, null);
    }

    private double calculateNextPrestigeCost(Prestige origin) {

        if (!this.increaseCostEnabled) {
            return origin.getCost();
        }

        double cost = origin.getId() == 0 ? (this.unlimitedPrestigeCost) : (origin.getCost() * this.increaseCostBy);

        return cost;
    }


    public Prestige getNextPrestige(Prestige prestige) {
        if (this.unlimitedPrestiges) {
            return new Prestige(prestige.getId() + 1, this.calculateNextPrestigeCost(prestige), this.unlimitedPrestigePrefix.replace("%prestige%", String.format("%,d", prestige.getId() + 1)), this.unlimitedPrestigesRewards.getOrDefault(prestige.getId() + 1, null));
        }
        return this.prestigeById.getOrDefault(prestige.getId() + 1, null);
    }

    public Rank getPlayerRank(Player p) {
        return this.ranksById.getOrDefault(this.onlinePlayersRanks.get(p.getUniqueId()), this.ranksById.get(1));
    }

    public Prestige getPlayerPrestige(Player p) {
        if (this.unlimitedPrestiges) {
            long prestige = this.onlinePlayersPrestige.getOrDefault(p.getUniqueId(), 0L);
            return new Prestige(prestige, this.calculatePrestigeCost(prestige), this.unlimitedPrestigePrefix.replace("%prestige%", String.format("%,d", prestige)), null);
        } else {
            return this.prestigeById.getOrDefault(this.onlinePlayersPrestige.get(p.getUniqueId()), this.prestigeById.get(0));
        }
    }

    private double calculatePrestigeCost(long prestige) {

        if (!this.increaseCostEnabled) {
            return this.unlimitedPrestigeCost;
        }

        double origin = this.unlimitedPrestigeCost;

        for (long i = 0; i < prestige; i++) {
            if (i == 0) {
                continue;
            }
            origin = origin * this.increaseCostBy;
        }

        return origin;
    }

    public boolean isMaxRank(Player p) {
        return this.getPlayerRank(p).getId() == this.maxRank.getId();
    }

    public boolean isMaxPrestige(Player p) {
        if (this.unlimitedPrestiges) {
            return this.getPlayerPrestige(p).getId() >= this.unlimitedPrestigeMax;
        }
        return this.getPlayerPrestige(p).getId() == this.maxPrestige.getId();
    }

    public boolean buyNextRank(Player p) {

        if (isMaxRank(p)) {
            p.sendMessage(this.plugin.getMessage("prestige_needed"));
            return false;
        }

        Rank currentRank = this.getPlayerRank(p);
        Rank toBuy = getNextRank(currentRank.getId());

        if (toBuy == null) {
            p.sendMessage(this.plugin.getMessage("prestige_needed"));
            return false;
        }

        if (!this.plugin.getCore().getEconomy().has(p, toBuy.getCost())) {
            p.sendMessage(this.plugin.getMessage("not_enough_money").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
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
        Prestige toBuy = getNextPrestige(currentPrestige);

        if (!this.plugin.getCore().getEconomy().has(p, toBuy.getCost())) {
            p.sendMessage(this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            return false;
        }

        doPrestige(p, toBuy);

        p.sendMessage(this.plugin.getMessage("prestige_up").replace("%Prestige%", toBuy.getPrefix()));

        return true;
    }

    public void sendPrestigeTop(CommandSender sender) {
        Schedulers.async().run(() -> {
            sender.sendMessage(Text.colorize(SPACER_LINE));
            if (this.updating) {
                sender.sendMessage(this.plugin.getMessage("top_updating"));
                sender.sendMessage(Text.colorize(SPACER_LINE_BOTTOM));
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
            sender.sendMessage(Text.colorize(SPACER_LINE_BOTTOM));
        });
    }

    private void updateTop10() {
        this.updating = true;
        task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            this.saveAllDataSync();
            this.updatePrestigeTop();
            this.updating = false;
		}, 30, TimeUnit.SECONDS, this.prestigeTopUpdateInterval, TimeUnit.MINUTES);
    }

    public void stopUpdating() {
        this.plugin.getCore().getLogger().info("Stopping updating Top 10 - Prestige");
        task.close();
    }

    private void updatePrestigeTop() {
        top10Prestige = new LinkedHashMap<>();
        this.plugin.getCore().getLogger().info("Starting updating PrestigeTop");

        this.top10Prestige = (LinkedHashMap<UUID, Integer>) this.plugin.getCore().getPluginDatabase().getTop10Prestiges();
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

        Prestige startPrestige = this.getPlayerPrestige(p);

        Prestige nextPrestige = this.getNextPrestige(startPrestige);

        if (!this.plugin.getCore().getEconomy().has(p, nextPrestige.getCost())) {
            p.sendMessage(this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", nextPrestige.getCost())));
            return false;
        }

        p.sendMessage(this.plugin.getMessage("max_prestige_started"));

        while (!isMaxPrestige(p) && isMaxRank(p) && this.plugin.getCore().getEconomy().has(p, nextPrestige.getCost())) {
            doPrestige(p, nextPrestige);
            nextPrestige = this.getNextPrestige(nextPrestige);
        }

        if (startPrestige.getId() < this.onlinePlayersPrestige.get(p.getUniqueId())) {
            p.sendMessage(this.plugin.getMessage("max_prestige_done").replace("%start_prestige%", String.format("%,d", startPrestige.getId())).replace("%prestige%", String.format("%,d", this.onlinePlayersPrestige.get(p.getUniqueId()))));
        }

        return true;
    }

    private void doPrestige(Player p, Prestige nextPrestige) {
        this.plugin.getCore().getEconomy().withdrawPlayer(p, nextPrestige.getCost());
        this.onlinePlayersPrestige.put(p.getUniqueId(), nextPrestige.getId());

        nextPrestige.runCommands(p);

        if (this.unlimitedPrestigesRewardPerPrestige != null) {
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.sync().run(() -> this.unlimitedPrestigesRewardPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()))));
            } else {
                this.unlimitedPrestigesRewardPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName())));
            }
        }

        if (this.resetRankAfterPrestige) {
            this.onlinePlayersRanks.put(p.getUniqueId(), 1);
        }
    }

    public void givePrestige(Player player, int levels) {

        if (!isMaxRank(player) || isMaxPrestige(player)) {
            return;
        }

        Prestige currentPrestige = this.getPlayerPrestige(player);

        long maxPrestige = this.unlimitedPrestiges ? this.unlimitedPrestigeMax : this.maxPrestige.getId();

        if (currentPrestige.getId() + levels > maxPrestige) {
            this.onlinePlayersPrestige.put(player.getUniqueId(), maxPrestige);
        } else {
            this.onlinePlayersPrestige.put(player.getUniqueId(), this.onlinePlayersPrestige.get(player.getUniqueId()) + levels);
        }


		player.sendMessage(this.plugin.getCore().getEnchants().getMessage("prestige_finder").replace("%prestige%", String.format("%,d", levels)));
    }


    public void addPlayerPrestige(CommandSender sender, Player target, int amount) {

        if (0 > amount) {
            return;
        }

        if (isMaxPrestige(target)) {
            return;
        }

        Prestige currentPrestige = this.getPlayerPrestige(target);

        long maxPrestige = this.unlimitedPrestiges ? this.unlimitedPrestigeMax : this.maxPrestige.getId();
        if (currentPrestige.getId() + amount > maxPrestige) {
            this.onlinePlayersPrestige.put(target.getUniqueId(), maxPrestige);
        } else {
            this.onlinePlayersPrestige.put(target.getUniqueId(), this.onlinePlayersPrestige.get(target.getUniqueId()) + amount);
        }

        sender.sendMessage(this.plugin.getMessage("prestige_add").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public void setPlayerPrestige(CommandSender sender, Player target, long amount) {

        if (0 > amount) {
            return;
        }

        long maxPretige = this.unlimitedPrestiges ? this.unlimitedPrestigeMax : this.maxPrestige.getId();

        if (amount > maxPretige) {
            this.onlinePlayersPrestige.put(target.getUniqueId(), maxPretige);
        } else {
            this.onlinePlayersPrestige.put(target.getUniqueId(), amount);
        }

        sender.sendMessage(this.plugin.getMessage("prestige_set").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public void removePlayerPrestige(CommandSender sender, Player target, long amount) {

        if (0 > amount) {
            return;
        }

        Prestige currentPrestige = this.getPlayerPrestige(target);

        if (currentPrestige.getId() - amount < 0) {
            this.onlinePlayersPrestige.put(target.getUniqueId(), 0L);
        } else {
            this.onlinePlayersPrestige.put(target.getUniqueId(), this.onlinePlayersPrestige.get(target.getUniqueId()) - amount);
        }

        sender.sendMessage(this.plugin.getMessage("prestige_add").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public Rank getRankById(int id) {
        return this.ranksById.get(id);
    }

    public boolean setRank(Player target, Rank rank, CommandSender sender) {

        Rank currentRank = this.getPlayerRank(target);

        rank.runCommands(target);

        this.onlinePlayersRanks.put(target.getUniqueId(), rank.getId());

        sender.sendMessage(this.plugin.getMessage("rank_set").replace("%rank%", rank.getPrefix()).replace("%player%", target.getName()));
        target.sendMessage(this.plugin.getMessage("rank_up").replace("%Rank-1%", currentRank.getPrefix()).replace("%Rank-2%", rank.getPrefix()));

        return true;
    }

    public int getRankupProgress(Player player) {

        if (this.isMaxRank(player)) {
            return getPrestigeProgress(player);
        }

        Rank current = this.getPlayerRank(player);
        Rank next = this.getNextRank(current.getId());

        int progress = (int) ((this.plugin.getCore().getEconomy().getBalance(player) / next.getCost()) * 100);

        if (progress > 100) {
            progress = 100;
        }

        return progress;
    }

    private int getPrestigeProgress(Player player) {
        if (this.isMaxPrestige(player)) {
            return 100;
        }

        Prestige current = this.getPlayerPrestige(player);
        Prestige next = this.getNextPrestige(current);

        int progress = (int) ((this.plugin.getCore().getEconomy().getBalance(player) / next.getCost()) * 100);

        if (progress > 100) {
            progress = 100;
        }

        return progress;
    }

    public double getNextRankCost(Player player) {
        if (this.isMaxRank(player)) {
            if (this.isMaxPrestige(player)) {
                return 0.0;
            } else {
                Prestige prestige = this.getPlayerPrestige(player);
                Prestige next = this.getNextPrestige(prestige);
                if (next != null) {
                    return next.getCost();
                } else {
                    return 0.0;
                }
            }
        }

        Rank current = this.getPlayerRank(player);
        return this.getNextRank(current.getId()).getCost();
    }
}
