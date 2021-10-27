package me.drawethree.ultraprisoncore.multipliers;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.multipliers.api.UltraPrisonMultipliersAPI;
import me.drawethree.ultraprisoncore.multipliers.api.UltraPrisonMultipliersAPIImpl;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import me.drawethree.ultraprisoncore.multipliers.multiplier.GlobalMultiplier;
import me.drawethree.ultraprisoncore.multipliers.multiplier.Multiplier;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text.Text;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class UltraPrisonMultipliers implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_Multipliers";
	public static final String TABLE_NAME_TOKEN = "UltraPrison_Token_Multipliers";

	public static final String MODULE_NAME = "Multipliers";

	@Getter
	private static UltraPrisonMultipliers instance;

	@Getter
	private FileManager.Config config;

	@Getter
	private UltraPrisonMultipliersAPI api;

	private GlobalMultiplier globalSellMultiplier;
	private GlobalMultiplier globalTokenMultiplier;
	private HashMap<UUID, Multiplier> rankMultipliers;
	private HashMap<UUID, PlayerMultiplier> sellMultipliers;
	private HashMap<UUID, PlayerMultiplier> tokenMultipliers;

	private HashMap<String, String> messages;
	private LinkedHashMap<String, Double> permissionToMultiplier;

	@Getter
	private UltraPrisonCore core;
	private boolean enabled;

	private Task rankUpdateTask;
	private int rankMultiplierUpdateTime;

	@Getter
	private double globalSellMultiMax;
	@Getter
	private double globalTokenMultiMax;
	@Getter
	private double playerSellMultiMax;
	@Getter
	private double playerTokenMultiMax;

	public UltraPrisonMultipliers(UltraPrisonCore UltraPrisonCore) {
		instance = this;
		this.core = UltraPrisonCore;
	}


	private void loadRankMultipliers() {
		this.permissionToMultiplier = new LinkedHashMap<>();

		ConfigurationSection section = getConfig().get().getConfigurationSection("ranks");

		if (section == null) {
			return;
		}

		boolean useLuckPerms = getConfig().get().getBoolean("use-luckperms-groups", false);

		String permPrefix = useLuckPerms ? "group." : "ultraprison.multiplier.";
		for (String rank : section.getKeys(false)) {
			String perm = permPrefix + rank;
			double multiplier = getConfig().get().getDouble("ranks." + rank);
			this.permissionToMultiplier.put(perm, multiplier);
			this.core.getLogger().info("Loaded rank multiplier." + rank + " with multiplier " + multiplier + " (" + perm + ")");
		}
	}


	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.config.reload();

		this.loadMessages();
		this.loadRankMultipliers();

		this.rankMultiplierUpdateTime = this.getConfig().get().getInt("rank-multiplier-update-time", 5);
		this.globalSellMultiMax = this.getConfig().get().getDouble("global-multiplier.sell.max", 10.0);
		this.globalTokenMultiMax = this.getConfig().get().getDouble("global-multiplier.tokens.max", 10.0);
		this.playerSellMultiMax = this.getConfig().get().getDouble("sell-multiplier.max", 10.0);
		this.playerTokenMultiMax = this.getConfig().get().getDouble("token-multiplier.max", 10.0);
	}

	@Override
	public void enable() {

		this.enabled = true;
		this.config = this.core.getFileManager().getConfig("multipliers.yml").copyDefaults(true).save();

		this.rankMultipliers = new HashMap<>();
		this.sellMultipliers = new HashMap<>();
		this.tokenMultipliers = new HashMap<>();

		this.rankMultiplierUpdateTime = this.getConfig().get().getInt("rank-multiplier-update-time", 5);
		this.globalSellMultiMax = this.getConfig().get().getDouble("global-multiplier.sell.max", 10.0);
		this.globalTokenMultiMax = this.getConfig().get().getDouble("global-multiplier.tokens.max", 10.0);
		this.playerSellMultiMax = this.getConfig().get().getDouble("sell-multiplier.max", 10.0);
		this.playerTokenMultiMax = this.getConfig().get().getDouble("token-multiplier.max", 10.0);

		this.loadMessages();
		this.loadRankMultipliers();
		this.registerCommands();
		this.registerEvents();
		this.removeExpiredMultipliers();
		this.loadGlobalMultipliers();
		this.loadOnlineMultipliers();
		api = new UltraPrisonMultipliersAPIImpl(this);

		this.rankUpdateTask = Schedulers.async().runRepeating(() -> {
			Players.all().forEach(p -> {
				this.rankMultipliers.put(p.getUniqueId(), this.calculateRankMultiplier(p));

			});
		}, this.rankMultiplierUpdateTime, TimeUnit.MINUTES, this.rankMultiplierUpdateTime, TimeUnit.MINUTES);
	}

	private void loadOnlineMultipliers() {
		Players.all().forEach(p -> {
			this.rankMultipliers.put(p.getUniqueId(), this.calculateRankMultiplier(p));
			this.loadSellMultiplier(p);
			this.loadTokenMultiplier(p);
		});
	}

	private void registerEvents() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.rankMultipliers.put(e.getPlayer().getUniqueId(), this.calculateRankMultiplier(e.getPlayer()));
					this.loadSellMultiplier(e.getPlayer());
					this.loadTokenMultiplier(e.getPlayer());
				}).bindWith(core);
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					this.rankMultipliers.remove(e.getPlayer().getUniqueId());
					this.saveSellMultiplier(e.getPlayer(), true);
					this.saveTokenMultiplier(e.getPlayer(), true);
				}).bindWith(core);
	}

	private void saveSellMultiplier(Player player, boolean async) {

		if (!this.sellMultipliers.containsKey(player.getUniqueId())) {
			return;
		}

		PlayerMultiplier multiplier = this.sellMultipliers.get(player.getUniqueId());

		if (async) {
			Schedulers.async().run(() -> {
				this.core.getPluginDatabase().saveSellMultiplier(player, multiplier);
				this.sellMultipliers.remove(player.getUniqueId());
				this.core.getLogger().info(String.format("Saved Sell Multiplier of player %s", player.getName()));
			});
		} else {
			this.core.getPluginDatabase().saveSellMultiplier(player, multiplier);
			this.sellMultipliers.remove(player.getUniqueId());
			this.core.getLogger().info(String.format("Saved Sell Multiplier of player %s", player.getName()));
		}
	}

	private void saveTokenMultiplier(Player player, boolean async) {
		if (!this.tokenMultipliers.containsKey(player.getUniqueId())) {
			return;
		}

		PlayerMultiplier multiplier = this.tokenMultipliers.get(player.getUniqueId());

		if (async) {
			Schedulers.async().run(() -> {
				this.core.getPluginDatabase().saveTokenMultiplier(player, multiplier);
				this.tokenMultipliers.remove(player.getUniqueId());
				this.core.getLogger().info(String.format("Saved Token Multiplier of player %s", player.getName()));
			});
		} else {
			this.core.getPluginDatabase().saveTokenMultiplier(player, multiplier);
			this.tokenMultipliers.remove(player.getUniqueId());
			this.core.getLogger().info(String.format("Saved Token Multiplier of player %s", player.getName()));
		}
	}

	private void loadGlobalMultipliers() {
		double multiSell = this.config.get().getDouble("global-multiplier.sell.multiplier");
		long timeLeftSell = this.config.get().getLong("global-multiplier.sell.timeLeft");

		double multiTokens = this.config.get().getDouble("global-multiplier.tokens.multiplier");
		long timeLeftTokens = this.config.get().getLong("global-multiplier.tokens.timeLeft");

		this.globalSellMultiplier = new GlobalMultiplier(0.0, 0, this.globalSellMultiMax);
		this.globalTokenMultiplier = new GlobalMultiplier(0.0, 0, this.globalTokenMultiMax);

		if (timeLeftSell > Time.nowMillis()) {
			this.globalSellMultiplier.setEndTime(timeLeftSell);
			this.globalSellMultiplier.setMultiplier(multiSell, this.globalSellMultiMax);
		} else {
			this.globalSellMultiplier.setEndTime(0);
			this.globalSellMultiplier.setMultiplier(0.0, this.globalSellMultiMax);
		}

		if (timeLeftTokens > Time.nowMillis()) {
			this.globalTokenMultiplier.setEndTime(timeLeftSell);
			this.globalTokenMultiplier.setMultiplier(multiTokens, this.globalTokenMultiMax);

		} else {
			this.globalTokenMultiplier.setEndTime(0);
			this.globalTokenMultiplier.setMultiplier(0.0, this.globalTokenMultiMax);
		}

		this.core.getLogger().info(String.format("Loaded Global Sell Multiplier %.2fx", multiSell));
		this.core.getLogger().info(String.format("Loaded Global Token Multiplier %.2fx", multiTokens));

	}

	private void saveGlobalMultipliers() {
		this.config.set("global-multiplier.sell.multiplier", this.globalSellMultiplier.getMultiplier());
		this.config.set("global-multiplier.sell.timeLeft", this.globalSellMultiplier.getEndTime());
		this.config.set("global-multiplier.tokens.multiplier", this.globalTokenMultiplier.getMultiplier());
		this.config.set("global-multiplier.tokens.timeLeft", this.globalTokenMultiplier.getEndTime());
		this.config.save();
		this.core.getLogger().info("Saved Global Multipliers into multipliers.yml");
	}

	private void loadSellMultiplier(Player player) {
		Schedulers.async().run(() -> {
			PlayerMultiplier multiplier = this.core.getPluginDatabase().getSellMultiplier(player);

			if (multiplier == null) {
				return;
			}

			this.sellMultipliers.put(player.getUniqueId(), multiplier);

			this.core.getLogger().info(String.format("Loaded Sell Multiplier %.2fx for player %s", multiplier.getMultiplier(), player.getName()));
		});
	}

	private void loadTokenMultiplier(Player player) {
		Schedulers.async().run(() -> {
			PlayerMultiplier multiplier = this.core.getPluginDatabase().getTokenMultiplier(player);

			if (multiplier == null) {
				return;
			}

			this.tokenMultipliers.put(player.getUniqueId(), multiplier);

			this.core.getLogger().info(String.format("Loaded Token Multiplier %.2fx for player %s", multiplier.getMultiplier(), player.getName()));
		});
	}

	private void removeExpiredMultipliers() {
		Schedulers.async().run(() -> {
			this.core.getPluginDatabase().removeExpiredMultipliers();
			this.core.getLogger().info("Removed expired multipliers from DB.");
		});
	}


	@Override
	public void disable() {
		this.saveAllMultipliers();
		this.rankUpdateTask.stop();
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME, TABLE_NAME_TOKEN};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case MYSQL:
			case SQLITE: {
				return new String[]{
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, sell_multiplier double, sell_multiplier_timeleft long, primary key (UUID))",
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TOKEN + "(UUID varchar(36) NOT NULL UNIQUE, token_multiplier double, token_multiplier_timeleft long, primary key (UUID))"
				};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	private void saveAllMultipliers() {
		Players.all().forEach(p -> {
			saveSellMultiplier(p, false);
			saveTokenMultiplier(p, false);
		});
		this.saveGlobalMultipliers();
	}


	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
		}
	}

	private void registerCommands() {
		Commands.create()
				.assertPermission("ultraprison.multipliers.admin")
				.handler(c -> {
					if (c.args().size() == 4) {
						String type = c.rawArg(0);
						double amount = c.arg(1).parseOrFail(Double.class);
						int duration = c.arg(2).parseOrFail(Integer.class);
						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ",")));
							return;
						}

						setupGlobalMultiplier(c.sender(), type, duration, timeUnit, amount);
					} else if (c.args().size() == 2 && c.rawArg(1).equalsIgnoreCase("reset")) {
						String type = c.rawArg(0);
						resetGlobalMultiplier(c.sender(), type);
					} else {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid usage!"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/gmulti <money/token> <multiplier> <time> <time_unit>"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/gmulti <money/token> reset"));
					}
				}).registerAndBind(core, "globalmultiplier", "gmulti");
		Commands.create()
				.assertPermission("ultraprison.multipliers.admin")
				.handler(c -> {
					if (c.args().size() == 4) {
						Player onlinePlayer = c.arg(0).parseOrFail(Player.class);
						double amount = c.arg(1).parseOrFail(Double.class);
						int duration = c.arg(2).parseOrFail(Integer.class);

						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ",")));
							return;
						}

						setupSellMultiplier(c.sender(), onlinePlayer, amount, timeUnit, duration);
					} else if (c.args().size() == 2 && c.rawArg(1).equalsIgnoreCase("reset")) {
						Player onlinePlayer = Players.getNullable(c.rawArg(0));
						resetSellMultiplier(c.sender(), onlinePlayer);
					} else {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid usage!"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/sellmulti <player> <multiplier> <time> <time_unit>"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/sellmulti <player> reset"));
					}
				}).registerAndBind(core, "sellmulti", "sellmultiplier", "smulti");
		Commands.create()
				.assertPermission("ultraprison.multipliers.admin")
				.handler(c -> {
					if (c.args().size() == 4) {
						Player onlinePlayer = c.arg(0).parseOrFail(Player.class);
						double amount = c.arg(1).parseOrFail(Double.class);
						int duration = c.arg(2).parseOrFail(Integer.class);
						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ",")));
							return;
						}
						setupTokenMultiplier(c.sender(), onlinePlayer, amount, timeUnit, duration);
					} else if (c.args().size() == 2 && c.rawArg(1).equalsIgnoreCase("reset")) {
						Player onlinePlayer = Players.getNullable(c.rawArg(0));
						resetTokenMultiplier(c.sender(), onlinePlayer);
					} else {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&cInvalid usage!"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/tokenmulti <player> <multiplier> <time> <time_unit>"));
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/tokenmulti <player> reset"));
					}
				}).registerAndBind(core, "tokenmulti", "tokenmultiplier", "tmulti");
		Commands.create()
				.assertPlayer()
				.handler(c -> {

					PlayerMultiplier sellMulti = this.getSellMultiplier(c.sender());
					PlayerMultiplier tokenMulti = this.getTokenMultiplier(c.sender());
					Multiplier rankMulti = this.getRankMultiplier(c.sender());

					PlayerUtils.sendMessage(c.sender(), messages.get("global_sell_multi").replace("%multiplier%", String.valueOf(this.globalSellMultiplier.getMultiplier())).replace("%duration%", this.globalSellMultiplier.getTimeLeftString()));
					PlayerUtils.sendMessage(c.sender(), messages.get("global_token_multi").replace("%multiplier%", String.valueOf(this.globalTokenMultiplier.getMultiplier())).replace("%duration%", this.globalTokenMultiplier.getTimeLeftString()));
					PlayerUtils.sendMessage(c.sender(), messages.get("rank_multi").replace("%multiplier%", rankMulti == null ? "0.0" : String.valueOf(rankMulti.getMultiplier())));
					PlayerUtils.sendMessage(c.sender(), messages.get("sell_multi").replace("%multiplier%", sellMulti == null || sellMulti.isExpired() ? "0.0" : String.valueOf(sellMulti.getMultiplier())).replace("%duration%", sellMulti == null || sellMulti.isExpired() ? "" : sellMulti.getTimeLeftString()));
					PlayerUtils.sendMessage(c.sender(), messages.get("token_multi").replace("%multiplier%", tokenMulti == null || tokenMulti.isExpired() ? "0.0" : String.valueOf(tokenMulti.getMultiplier())).replace("%duration%", tokenMulti == null || tokenMulti.isExpired() ? "" : tokenMulti.getTimeLeftString()));
				}).registerAndBind(core, "multiplier", "multi");
	}

	private void resetGlobalMultiplier(CommandSender sender, String type) {
		switch (type.toLowerCase()) {
			case "sell":
			case "money":
				this.globalSellMultiplier.reset();
				PlayerUtils.sendMessage(sender, Text.colorize("&eGlobal Sell Multiplier &awas reset."));
				break;
			case "tokens":
			case "token":
				this.globalTokenMultiplier.reset();
				PlayerUtils.sendMessage(sender, Text.colorize("&eGlobal Token Multiplier &awas reset."));
				break;
		}
	}

	private void resetSellMultiplier(CommandSender sender, Player onlinePlayer) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&cPlayer must be online!"));
			return;
		}

		if (sellMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			PlayerMultiplier multiplier = sellMultipliers.get(onlinePlayer.getUniqueId());
			multiplier.reset();
			PlayerUtils.sendMessage(sender, Text.colorize(String.format("&aYou have reset &e%s's &eSell Multiplier.", onlinePlayer.getName())));
			PlayerUtils.sendMessage(onlinePlayer, messages.get("sell_multi_reset"));
		} else {
			PlayerUtils.sendMessage(sender, Text.colorize(String.format("&cCould not fetch the &e%s's &eSell Multiplier.", onlinePlayer.getName())));
		}
	}

	private void resetTokenMultiplier(CommandSender sender, Player onlinePlayer) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&cPlayer must be online!"));
			return;
		}

		if (tokenMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			PlayerMultiplier multiplier = tokenMultipliers.get(onlinePlayer.getUniqueId());
			multiplier.reset();
			PlayerUtils.sendMessage(sender, Text.colorize(String.format("&aYou have reset &e%s's &eToken Multiplier.", onlinePlayer.getName())));
			PlayerUtils.sendMessage(onlinePlayer, messages.get("token_multi_reset"));
		} else {
			PlayerUtils.sendMessage(sender, Text.colorize(String.format("&cCould not fetch the &e%s's &eToken Multiplier.", onlinePlayer.getName())));
		}
	}

	private void setupSellMultiplier(CommandSender sender, Player onlinePlayer, double amount, TimeUnit timeUnit, int duration) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&cPlayer must be online!"));
			return;
		}

		if (sellMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			PlayerMultiplier multiplier = sellMultipliers.get(onlinePlayer.getUniqueId());
			multiplier.addMultiplier(amount, this.playerSellMultiMax);
			multiplier.addDuration(timeUnit, duration);
			sellMultipliers.put(onlinePlayer.getUniqueId(), multiplier);
		} else {
			sellMultipliers.put(onlinePlayer.getUniqueId(), new PlayerMultiplier(onlinePlayer.getUniqueId(), amount, timeUnit, duration, this.playerSellMultiMax));
		}

		PlayerUtils.sendMessage(onlinePlayer, messages.get("sell_multi_apply").replace("%multiplier%", String.valueOf(amount)).replace("%time%", duration + " " + StringUtils.capitalize(timeUnit.name())));
		PlayerUtils.sendMessage(sender, Text.colorize(String.format("&aYou have set &e%s's &eSell Multiplier &ato &e%.2f &afor &e%d &a%s.", onlinePlayer.getName(), amount, duration, StringUtils.capitalize(timeUnit.name()))));
	}

	private void setupTokenMultiplier(CommandSender sender, Player onlinePlayer, double amount, TimeUnit timeUnit, int minutes) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&cPlayer must be online!"));
			return;
		}

		if (tokenMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			PlayerMultiplier multiplier = tokenMultipliers.get(onlinePlayer.getUniqueId());
			multiplier.addMultiplier(amount, this.playerTokenMultiMax);
			multiplier.addDuration(TimeUnit.MINUTES, minutes);
			tokenMultipliers.put(onlinePlayer.getUniqueId(), multiplier);
		} else {
			tokenMultipliers.put(onlinePlayer.getUniqueId(), new PlayerMultiplier(onlinePlayer.getUniqueId(), amount, TimeUnit.MINUTES, minutes, this.playerTokenMultiMax));
		}

		PlayerUtils.sendMessage(onlinePlayer, messages.get("token_multi_apply").replace("%multiplier%", String.valueOf(amount)).replace("%time%", minutes + " " + StringUtils.capitalize(timeUnit.name())));
		PlayerUtils.sendMessage(sender, Text.colorize(String.format("&aYou have set &e%s's &eToken Multiplier &ato &e%.2f &afor &e%d &a%s.", onlinePlayer.getName(), amount, minutes, StringUtils.capitalize(timeUnit.name()))));
	}


	private void setupGlobalMultiplier(CommandSender sender, String type, int time, TimeUnit timeUnit, double amount) {
		switch (type.toLowerCase()) {
			case "sell":
			case "money":
				this.globalSellMultiplier.addMultiplier(amount, this.globalSellMultiMax);
				this.globalSellMultiplier.addDuration(timeUnit, time);
				PlayerUtils.sendMessage(sender, Text.colorize(String.format("&aYou have set the &eGlobal Sell Multiplier &ato &e%.2f &afor &e%d &a%s.", amount, time, StringUtils.capitalize(timeUnit.name()))));
				break;
			case "tokens":
			case "token":
				this.globalTokenMultiplier.addMultiplier(amount, this.globalTokenMultiMax);
				this.globalTokenMultiplier.addDuration(timeUnit, time);
				PlayerUtils.sendMessage(sender, Text.colorize(String.format("&aYou have set the &eGlobal Token Multiplier &ato &e%.2f &afor &e%d &a%s.", amount, time, StringUtils.capitalize(timeUnit.name()))));
				break;
		}
	}


	public GlobalMultiplier getGlobalSellMultiplier() {
		return this.globalSellMultiplier;
	}

	public GlobalMultiplier getGlobalTokenMultiplier() {
		return this.globalTokenMultiplier;
	}

	public PlayerMultiplier getSellMultiplier(Player p) {
		return sellMultipliers.get(p.getUniqueId());
	}

	public PlayerMultiplier getTokenMultiplier(Player p) {
		return tokenMultipliers.get(p.getUniqueId());
	}

	public Multiplier getRankMultiplier(Player p) {
		return rankMultipliers.get(p.getUniqueId());
	}

	public void removeSellMultiplier(UUID uuid) {
		sellMultipliers.remove(uuid);
	}

	public void removeTokenMultiplier(UUID uuid) {
		tokenMultipliers.remove(uuid);
	}


	private Multiplier calculateRankMultiplier(Player p) {
		PlayerMultiplier toReturn = new PlayerMultiplier(p.getUniqueId(), 0.0, -1, MultiplierType.SELL);

		for (String perm : this.permissionToMultiplier.keySet()) {
			if (p.hasPermission(perm)) {
				toReturn.addMultiplier(this.permissionToMultiplier.get(perm), Integer.MAX_VALUE);
				break;
			}
		}

		return toReturn;
	}


}
