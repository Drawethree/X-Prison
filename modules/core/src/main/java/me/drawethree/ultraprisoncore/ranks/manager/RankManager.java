package me.drawethree.ultraprisoncore.ranks.manager;

import me.drawethree.ultraprisoncore.api.enums.LostCause;
import me.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.drawethree.ultraprisoncore.prestiges.manager.PrestigeManager;
import me.drawethree.ultraprisoncore.prestiges.model.Prestige;
import me.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import me.drawethree.ultraprisoncore.ranks.api.events.PlayerRankUpEvent;
import me.drawethree.ultraprisoncore.ranks.model.Rank;
import me.drawethree.ultraprisoncore.utils.misc.ProgressBar;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.drawethree.ultraprisoncore.utils.text.TextUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class RankManager {

	private UltraPrisonRanks plugin;
	private Map<Integer, Rank> ranksById;
	private Map<UUID, Integer> onlinePlayersRanks = new HashMap<>();

	private Rank maxRank;

	private boolean useTokensCurrency;

	public RankManager(UltraPrisonRanks plugin) {
		this.plugin = plugin;
		this.reload();

		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.addIntoTable(e.getPlayer());
					loadPlayerRank(e.getPlayer());
				}).bindWith(plugin.getCore());
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					savePlayerRank(e.getPlayer());
				}).bindWith(plugin.getCore());

	}

	private void addIntoTable(Player player) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getPluginDatabase().addIntoRanks(player);
		});
	}

	public void saveAllDataSync() {
		for (UUID uuid : this.onlinePlayersRanks.keySet()) {
			this.plugin.getCore().getPluginDatabase().updateRank(Players.getOfflineNullable(uuid), onlinePlayersRanks.get(uuid));
		}
		this.plugin.getCore().getLogger().info("Saved players ranks.");
	}

	public void loadAllData() {
		for (Player p : Players.all()) {
			loadPlayerRank(p);
		}
	}

	private void savePlayerRank(Player player) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getPluginDatabase().updateRank(player, this.getPlayerRank(player).getId());
			this.onlinePlayersRanks.remove(player.getUniqueId());
			this.plugin.getCore().getLogger().info("Saved " + player.getName() + "'s rank.");
		});
	}

	private void loadPlayerRank(Player player) {
		Schedulers.async().run(() -> {

			int rank = this.plugin.getCore().getPluginDatabase().getPlayerRank(player);

			this.onlinePlayersRanks.put(player.getUniqueId(), rank);
			this.plugin.getCore().getLogger().info("Loaded " + player.getName() + "'s rank.");

		});
	}

	public void reload() {
		this.useTokensCurrency = plugin.getConfig().get().getBoolean("use_tokens_currency");

		this.plugin.getCore().getLogger().info("Using " + (useTokensCurrency ? "Tokens" : "Money") + " currency for Ranks.");

		this.loadRanks();
	}

	private void loadRanks() {
		this.ranksById = new LinkedHashMap<>();
		for (String key : plugin.getConfig().get().getConfigurationSection("Ranks").getKeys(false)) {
			int id = Integer.parseInt(key);
			String prefix = TextUtils.applyColor(plugin.getConfig().get().getString("Ranks." + key + ".Prefix"));
			long cost = plugin.getConfig().get().getLong("Ranks." + key + ".Cost");
			List<String> commands = plugin.getConfig().get().getStringList("Ranks." + key + ".CMD");

			Rank rank = new Rank(id, cost, prefix, commands);
			this.ranksById.put(id, rank);
			this.maxRank = rank;
		}
		this.plugin.getCore().getLogger().info(String.format("Loaded %d ranks!", ranksById.keySet().size()));
	}

	public Rank getNextRank(int id) {
		return this.ranksById.getOrDefault(id + 1, null);
	}

	public Rank getPreviousRank(int id) {
		return this.ranksById.getOrDefault(id - 1, null);
	}

	public synchronized Rank getPlayerRank(Player p) {
		return this.ranksById.getOrDefault(this.onlinePlayersRanks.get(p.getUniqueId()), this.ranksById.get(1));
	}

	public boolean isMaxRank(Player p) {
		return this.getPlayerRank(p).getId() == this.maxRank.getId();
	}

	public Rank getRankById(int id) {
		return this.ranksById.get(id);
	}

	public boolean buyMaxRank(Player p) {

		if (isMaxRank(p)) {
			PlayerUtils.sendMessage(p, this.plugin.getMessage("prestige_needed"));
			return false;
		}

		Rank currentRank = this.getPlayerRank(p);

		int finalRankId = currentRank.getId();

		for (int i = currentRank.getId(); i < maxRank.getId(); i++) {
			double cost = this.getRankById(i + 1).getCost();
			if (!this.isTransactionAllowed(p, cost)) {
				break;
			}
			if (!this.completeTransaction(p, cost)) {
				break;
			}
			finalRankId = i + 1;
		}

		if (finalRankId == currentRank.getId()) {
			PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_money").replace("%cost%", String.format("%,.0f", this.getNextRank(currentRank.getId()).getCost())));
			return false;
		}

		Rank finalRank = this.getRankById(finalRankId);

		PlayerRankUpEvent event = new PlayerRankUpEvent(p, currentRank, finalRank);

		Events.call(event);

		if (event.isCancelled()) {
			this.plugin.getCore().debug("PlayerRankUpEvent was cancelled.", this.plugin);
			return false;
		}

		for (int i = currentRank.getId() + 1; i <= finalRank.getId(); i++) {
			this.ranksById.get(i).runCommands(p);
		}

		this.onlinePlayersRanks.put(p.getUniqueId(), finalRank.getId());
		PlayerUtils.sendMessage(p, this.plugin.getMessage("rank_up").replace("%Rank-1%", currentRank.getPrefix()).replace("%Rank-2%", finalRank.getPrefix()));
		return true;
	}

	public boolean buyNextRank(Player p) {

		if (isMaxRank(p)) {
			PlayerUtils.sendMessage(p, this.plugin.getMessage("prestige_needed"));
			return false;
		}

		Rank currentRank = this.getPlayerRank(p);
		Rank toBuy = getNextRank(currentRank.getId());

		if (toBuy == null) {
			PlayerUtils.sendMessage(p, this.plugin.getMessage("prestige_needed"));
			return false;
		}

		if (!this.isTransactionAllowed(p, toBuy.getCost())) {
			if (this.useTokensCurrency) {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_tokens").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
			} else {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_money").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
			}
			return false;
		}

		PlayerRankUpEvent event = new PlayerRankUpEvent(p, currentRank, toBuy);

		Events.call(event);

		if (event.isCancelled()) {
			this.plugin.getCore().debug("PlayerRankUpEvent was cancelled.", this.plugin);
			return false;
		}

		if (!this.completeTransaction(p, toBuy.getCost())) {
			return false;
		}

		toBuy.runCommands(p);

		this.onlinePlayersRanks.put(p.getUniqueId(), toBuy.getId());

		PlayerUtils.sendMessage(p, this.plugin.getMessage("rank_up").replace("%Rank-1%", currentRank.getPrefix()).replace("%Rank-2%", toBuy.getPrefix()));
		return true;
	}

	private boolean completeTransaction(Player p, double cost) {
		if (this.useTokensCurrency) {
			this.plugin.getCore().getTokens().getApi().removeTokens(p, (long) cost, LostCause.RANKUP);
			return true;
		} else {
			return this.plugin.getCore().getEconomy().withdrawPlayer(p, cost).transactionSuccess();
		}
	}

	private boolean isTransactionAllowed(Player p, double cost) {
		if (this.useTokensCurrency) {
			if (!this.plugin.getCore().getTokens().getApi().hasEnough(p, (long) cost)) {
				return false;
			}
			return true;
		} else {
			if (!this.plugin.getCore().getEconomy().has(p, cost)) {
				return false;
			}
			return true;
		}

	}


	public boolean setRank(Player target, Rank rank, CommandSender sender) {

		Rank currentRank = this.getPlayerRank(target);

		PlayerRankUpEvent event = new PlayerRankUpEvent(target, currentRank, rank);

		Events.call(event);

		if (event.isCancelled()) {
			this.plugin.getCore().debug("PlayerRankUpEvent was cancelled.", this.plugin);
			return false;
		}

		rank.runCommands(target);

		this.onlinePlayersRanks.put(target.getUniqueId(), rank.getId());

		if (sender != null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("rank_set").replace("%rank%", rank.getPrefix()).replace("%player%", target.getName()));
			PlayerUtils.sendMessage(target, this.plugin.getMessage("rank_up").replace("%Rank-1%", currentRank.getPrefix()).replace("%Rank-2%", rank.getPrefix()));
		}

		return true;
	}

	public int getRankupProgress(Player player) {

		if (this.isMaxRank(player)) {
			if (arePrestigesEnabled()) {
				return getPrestigeManager().getPrestigeProgress(player);
			}
			return 100;
		}

		Rank current = this.getPlayerRank(player);
		Rank next = this.getNextRank(current.getId());

		double currentBalance = this.useTokensCurrency ? this.plugin.getCore().getTokens().getApi().getPlayerTokens(player) : this.plugin.getCore().getEconomy().getBalance(player);

		int progress = (int) ((currentBalance / next.getCost()) * 100);

		if (progress > 100) {
			progress = 100;
		}

		return progress;
	}

	public double getNextRankCost(Player player) {
		if (this.isMaxRank(player)) {
			if (arePrestigesEnabled()) {
				if (getPrestigeManager().isMaxPrestige(player)) {
					return 0.0;
				} else {
					Prestige prestige = getPrestigeManager().getPlayerPrestige(player);
					Prestige next = getPrestigeManager().getNextPrestige(prestige);
					if (next != null) {
						return next.getCost();
					} else {
						return 0.0;
					}
				}
			} else {
				return 0.0;
			}
		}

		Rank current = this.getPlayerRank(player);
		return this.getNextRank(current.getId()).getCost();
	}

	public void resetPlayerRank(Player p) {
		this.onlinePlayersRanks.put(p.getUniqueId(), 1);
	}

	private boolean arePrestigesEnabled() {
		return this.plugin.getCore().isModuleEnabled(UltraPrisonPrestiges.MODULE_NAME);
	}

	private PrestigeManager getPrestigeManager() {
		if (!arePrestigesEnabled()) {
			throw new IllegalStateException("Prestiges module is not enabled");
		}
		return this.plugin.getCore().getPrestiges().getPrestigeManager();
	}

	public String getRankupProgressBar(Player player) {

		double currentProgress, required = 100;
		if (this.isMaxRank(player)) {
			currentProgress = 100;
			if (arePrestigesEnabled()) {
				currentProgress = getPrestigeManager().getPrestigeProgress(player);
			}
		} else {
			Rank current = this.getPlayerRank(player);
			Rank next = this.getNextRank(current.getId());
			double currentBalance = this.useTokensCurrency ? this.plugin.getCore().getTokens().getApi().getPlayerTokens(player) : this.plugin.getCore().getEconomy().getBalance(player);
			currentProgress = (currentBalance / next.getCost()) * 100;
		}

		if (currentProgress > 100) {
			currentProgress = 100;
		}

		return ProgressBar.getProgressBar(20, ":", currentProgress, required);
	}
}
