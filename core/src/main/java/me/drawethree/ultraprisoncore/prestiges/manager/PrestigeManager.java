package me.drawethree.ultraprisoncore.prestiges.manager;

import me.drawethree.ultraprisoncore.api.enums.LostCause;
import me.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.drawethree.ultraprisoncore.prestiges.api.events.PlayerPrestigeEvent;
import me.drawethree.ultraprisoncore.prestiges.model.Prestige;
import me.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import me.drawethree.ultraprisoncore.ranks.manager.RankManager;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
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

public class PrestigeManager {

	private UltraPrisonPrestiges plugin;

	private Map<Long, Prestige> prestigeById;

	private Map<UUID, Long> onlinePlayersPrestige = new HashMap<>();

	private List<UUID> prestigingPlayers;

	private Prestige maxPrestige;

	private List<String> prestigeTopFormat;
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
	private List<String> unlimitedPrestigesRewardPerPrestige;
	private boolean useTokensCurrency;

	public PrestigeManager(UltraPrisonPrestiges plugin) {
		this.plugin = plugin;
		this.prestigingPlayers = new ArrayList<>(10);
		this.reload();

		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.addIntoTable(e.getPlayer());
					loadPlayerPrestige(e.getPlayer());
				}).bindWith(plugin.getCore());
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					savePlayerPrestige(e.getPlayer());
				}).bindWith(plugin.getCore());

		this.updateTop10();
	}

	private void addIntoTable(Player player) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getPluginDatabase().addIntoPrestiges(player);
		});
	}

	public void saveAllDataSync() {
		for (UUID uuid : this.onlinePlayersPrestige.keySet()) {
			this.plugin.getCore().getPluginDatabase().updatePrestige(Players.getOfflineNullable(uuid), onlinePlayersPrestige.get(uuid));
		}
		this.plugin.getCore().getLogger().info("Saved players prestiges.");
	}

	public void loadAllData() {
		for (Player p : Players.all()) {
			loadPlayerPrestige(p);
		}
	}

	private void savePlayerPrestige(Player player) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getPluginDatabase().updatePrestige(player, this.getPlayerPrestige(player).getId());
			this.onlinePlayersPrestige.remove(player.getUniqueId());
			this.plugin.getCore().getLogger().info("Saved " + player.getName() + "'s prestige to database.");
		});
	}

	private void loadPlayerPrestige(Player player) {
		Schedulers.async().run(() -> {
			long prestige = this.plugin.getCore().getPluginDatabase().getPlayerPrestige(player);
			this.onlinePlayersPrestige.put(player.getUniqueId(), prestige);
			this.plugin.getCore().getLogger().info("Loaded " + player.getName() + "'s prestige.");

		});
	}

	public void reload() {
		this.prestigeTopFormat = plugin.getConfig().get().getStringList("prestige-top-format");
		this.unlimitedPrestiges = plugin.getConfig().get().getBoolean("unlimited_prestiges.enabled");
		this.unlimitedPrestigeCost = plugin.getConfig().get().getDouble("unlimited_prestiges.prestige_cost");
		this.unlimitedPrestigePrefix = Text.colorize(plugin.getConfig().get().getString("unlimited_prestiges.prefix"));
		this.unlimitedPrestigeMax = plugin.getConfig().get().getLong("unlimited_prestiges.max_prestige");

		this.increaseCostEnabled = plugin.getConfig().get().getBoolean("unlimited_prestiges.increase_cost.enabled");

		this.increaseCostBy = plugin.getConfig().get().getDouble("unlimited_prestiges.increase_cost.increase_cost_by");
		boolean unlimitedPrestigesRewardPerPrestigeEnabled = plugin.getConfig().get().getBoolean("unlimited_prestiges.rewards-per-prestige.enabled");

		if (unlimitedPrestigesRewardPerPrestigeEnabled) {
			this.unlimitedPrestigesRewardPerPrestige = plugin.getConfig().get().getStringList("unlimited_prestiges.rewards-per-prestige.rewards");
		}

		this.resetRankAfterPrestige = plugin.getConfig().get().getBoolean("reset_rank_after_prestige");

		this.prestigeTopUpdateInterval = plugin.getConfig().get().getInt("prestige_top_update_interval");
		this.useTokensCurrency = plugin.getConfig().get().getBoolean("use_tokens_currency");

		this.plugin.getCore().getLogger().info("Using " + (useTokensCurrency ? "Tokens" : "Money") + " currency for Prestiges.");


		this.loadUnlimitedPrestigesRewards();

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

	private double calculateNextPrestigeCost(Prestige origin) {

		if (!this.increaseCostEnabled) {
			return origin.getCost();
		}

		return origin.getId() == 0 ? (this.unlimitedPrestigeCost) : (origin.getCost() * this.increaseCostBy);
	}


	public Prestige getNextPrestige(Prestige prestige) {
		if (this.unlimitedPrestiges) {
			return new Prestige(prestige.getId() + 1, this.calculateNextPrestigeCost(prestige), this.unlimitedPrestigePrefix.replace("%prestige%", String.format("%,d", prestige.getId() + 1)), this.unlimitedPrestigesRewards.getOrDefault(prestige.getId() + 1, null));
		}
		return this.prestigeById.getOrDefault(prestige.getId() + 1, null);
	}

	public synchronized Prestige getPlayerPrestige(Player p) {
		if (this.unlimitedPrestiges) {
			long prestige = this.onlinePlayersPrestige.getOrDefault(p.getUniqueId(), 0L);
			return new Prestige(prestige, this.calculatePrestigeCost(prestige), this.unlimitedPrestigePrefix.replace("%prestige%", String.format("%,d", prestige)), null);
		} else {
			return this.prestigeById.getOrDefault(this.onlinePlayersPrestige.get(p.getUniqueId()), this.prestigeById.get(0L));
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

	public boolean isMaxPrestige(Player p) {
		if (this.unlimitedPrestiges) {
			return this.getPlayerPrestige(p).getId() >= this.unlimitedPrestigeMax;
		}
		return this.getPlayerPrestige(p).getId() == this.maxPrestige.getId();
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

	public boolean buyNextPrestige(Player p) {

		if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
			PlayerUtils.sendMessage(p, this.plugin.getMessage("not_last_rank"));
			return false;
		}

		if (isMaxPrestige(p)) {
			PlayerUtils.sendMessage(p, this.plugin.getMessage("last_prestige"));
			return false;
		}

		Prestige currentPrestige = this.getPlayerPrestige(p);
		Prestige toBuy = getNextPrestige(currentPrestige);

		if (!this.isTransactionAllowed(p, toBuy.getCost())) {
			if (this.useTokensCurrency) {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_tokens_prestige").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
			} else {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
			}
			return false;
		}

		PlayerPrestigeEvent event = new PlayerPrestigeEvent(p, currentPrestige, toBuy);

		Events.call(event);

		if (event.isCancelled()) {
			this.plugin.getCore().debug("PlayerPrestigeEvent was cancelled.", this.plugin);
			return false;
		}

		doPrestige(p, toBuy);

		PlayerUtils.sendMessage(p, this.plugin.getMessage("prestige_up").replace("%Prestige%", toBuy.getPrefix()));

		return true;
	}

	public void sendPrestigeTop(CommandSender sender) {
		if (this.updating) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("top_updating"));
			return;
		}

		for (String s : this.prestigeTopFormat) {
			if (s.startsWith("{FOR_EACH_PLAYER}")) {
				String rawContent = s.replace("{FOR_EACH_PLAYER} ", "");
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
						PlayerUtils.sendMessage(sender, Text.colorize(rawContent.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%prestige%", String.format("%,d", prestige))));
					} catch (Exception e) {
						break;
					}
				}
			} else {
				PlayerUtils.sendMessage(sender, me.lucko.helper.text3.Text.colorize(s));
			}
		}
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

	public void buyMaxPrestige(Player p) {

		Schedulers.async().run(() -> {
			if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("not_last_rank"));
				return;
			}

			if (isMaxPrestige(p)) {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("last_prestige"));
				return;
			}

			Prestige startPrestige = this.getPlayerPrestige(p);

			Prestige currentPrestige = startPrestige;

			Prestige nextPrestige = this.getNextPrestige(startPrestige);

			if (!this.isTransactionAllowed(p, nextPrestige.getCost())) {
				if (this.useTokensCurrency) {
					PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_tokens_prestige").replace("%cost%", String.format("%,.0f", nextPrestige.getCost())));
				} else {
					PlayerUtils.sendMessage(p, this.plugin.getMessage("not_enough_money_prestige").replace("%cost%", String.format("%,.0f", nextPrestige.getCost())));
				}
				return;
			}

			PlayerUtils.sendMessage(p, this.plugin.getMessage("max_prestige_started"));

			this.prestigingPlayers.add(p.getUniqueId());
			while (p.isOnline() && !isMaxPrestige(p) && this.isTransactionAllowed(p, nextPrestige.getCost())) {

				if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
					break;
				}

				doPrestige(p, nextPrestige);
				currentPrestige = nextPrestige;
				nextPrestige = this.getNextPrestige(nextPrestige);
			}

			PlayerPrestigeEvent event = new PlayerPrestigeEvent(p, startPrestige, currentPrestige);

			Events.callSync(event);

			this.prestigingPlayers.remove(p.getUniqueId());

			if (startPrestige.getId() < this.onlinePlayersPrestige.get(p.getUniqueId())) {
				PlayerUtils.sendMessage(p, this.plugin.getMessage("max_prestige_done").replace("%start_prestige%", String.format("%,d", startPrestige.getId())).replace("%prestige%", String.format("%,d", this.onlinePlayersPrestige.get(p.getUniqueId()))));
			}
		});
	}

	private void doPrestige(Player p, Prestige nextPrestige) {

		if (!this.completeTransaction(p, nextPrestige.getCost())) {
			return;
		}

		this.onlinePlayersPrestige.put(p.getUniqueId(), nextPrestige.getId());

		nextPrestige.runCommands(p);

		if (this.unlimitedPrestigesRewardPerPrestige != null) {
			if (!Bukkit.isPrimaryThread()) {
				Schedulers.sync().run(() -> this.unlimitedPrestigesRewardPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()))));
			} else {
				this.unlimitedPrestigesRewardPerPrestige.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName())));
			}
		}

		if (this.resetRankAfterPrestige && areRanksEnabled()) {
			getRankManager().resetPlayerRank(p);
		}
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

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("prestige_add").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
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

		if (sender != null) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("prestige_set").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
		}
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

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("prestige_remove").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
	}

	public int getPrestigeProgress(Player player) {

		if (this.isMaxPrestige(player)) {
			return 100;
		}

		Prestige current = this.getPlayerPrestige(player);
		Prestige next = this.getNextPrestige(current);

		double currentBalance = this.useTokensCurrency ? this.plugin.getCore().getTokens().getApi().getPlayerTokens(player) : this.plugin.getCore().getEconomy().getBalance(player);

		int progress = (int) ((currentBalance / next.getCost()) * 100);

		if (progress > 100) {
			progress = 100;
		}

		return progress;
	}

	private boolean areRanksEnabled() {
		return this.plugin.getCore().isModuleEnabled(UltraPrisonRanks.MODULE_NAME);
	}

	private RankManager getRankManager() {
		if (!areRanksEnabled()) {
			throw new IllegalStateException("Ranks module is not enabled");
		}
		return this.plugin.getCore().getRanks().getRankManager();
	}

	public boolean isPrestiging(Player sender) {
		return this.prestigingPlayers.contains(sender.getUniqueId());
	}
}
