package dev.drawethree.xprison.multipliers;

import dev.drawethree.xprison.XPrisonLite;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplierBase;
import dev.drawethree.xprison.multipliers.repo.MultipliersRepository;
import dev.drawethree.xprison.multipliers.repo.impl.MultipliersRepositoryImpl;
import dev.drawethree.xprison.multipliers.service.MultipliersService;
import dev.drawethree.xprison.multipliers.service.impl.MultipliersServiceImpl;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public final class XPrisonMultipliers extends XPrisonModuleBase implements PlayerDataHolder {

	public static final String MODULE_NAME = "Multipliers";

	@Getter
	private static XPrisonMultipliers instance;
	@Getter
	private FileManager.Config config;

	private Map<UUID, PlayerMultiplierBase> sellMultipliers;
	private Map<UUID, PlayerMultiplierBase> tokenMultipliers;

	private Map<String, String> messages;
	private Map<String, Double> permissionToMultiplier;

	@Getter
	private double playerSellMultiMax;
	@Getter
	private double playerTokenMultiMax;

	@Getter
	private MultipliersRepository multipliersRepository;

	@Getter
	private MultipliersService multipliersService;

	public XPrisonMultipliers(XPrisonLite plugin) {
		super(plugin);
		instance = this;
	}


	private void loadRankMultipliers() {
		this.permissionToMultiplier = new LinkedHashMap<>();

		ConfigurationSection section = getConfig().get().getConfigurationSection("ranks");

		if (section == null) {
			return;
		}

		boolean useLuckPerms = getConfig().get().getBoolean("use-luckperms-groups", false);

		String permPrefix = useLuckPerms ? "group." : "xprison.multiplier.";

		for (String rank : section.getKeys(false)) {
			String perm = permPrefix + rank;
			double multiplier = getConfig().get().getDouble("ranks." + rank);
			this.permissionToMultiplier.put(perm, multiplier);
			this.core.debug("Loaded Rank Multiplier '" + rank + "' with multiplier x" + multiplier + " (" + perm + ")", this);
		}
	}

	@Override
	public void reload() {
		super.reload();
		this.config.reload();

		this.loadMessages();
		this.loadRankMultipliers();

		this.playerSellMultiMax = this.getConfig().get().getDouble("sell-multiplier.max", 10.0);
		this.playerTokenMultiMax = this.getConfig().get().getDouble("token-multiplier.max", 10.0);
	}

	@Override
	public void enable() {
		super.enable();
		this.config = this.core.getFileManager().getConfig("multipliers.yml").copyDefaults(true).save();

		this.sellMultipliers = new ConcurrentHashMap<>();
		this.tokenMultipliers = new ConcurrentHashMap<>();

		this.multipliersRepository = new MultipliersRepositoryImpl(this.core.getPluginDatabase());
		this.multipliersRepository.createTables();
		this.multipliersRepository.removeExpiredMultipliers();

		this.multipliersService = new MultipliersServiceImpl(this.multipliersRepository);

		this.playerSellMultiMax = this.getConfig().get().getDouble("sell-multiplier.max", 10.0);
		this.playerTokenMultiMax = this.getConfig().get().getDouble("token-multiplier.max", 10.0);

		this.loadMessages();
		this.loadRankMultipliers();
		this.registerCommands();
		this.registerEvents();
		this.removeExpiredMultipliers();
		this.loadOnlineMultipliers();

		this.enabled = true;
	}

	private void loadOnlineMultipliers() {
		Players.all().forEach(p -> {
			this.loadSellMultiplier(p);
			this.loadTokenMultiplier(p);
		});
	}

	private void registerEvents() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.loadSellMultiplier(e.getPlayer());
					this.loadTokenMultiplier(e.getPlayer());
				}).bindWith(this);
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					this.saveSellMultiplier(e.getPlayer(), true);
					this.saveTokenMultiplier(e.getPlayer(), true);
				}).bindWith(this);
	}

	private void saveSellMultiplier(Player player, boolean async) {

		PlayerMultiplierBase multiplier = this.sellMultipliers.remove(player.getUniqueId());

		if (async) {
			Schedulers.async().run(() -> {
				this.multipliersService.setSellMultiplier(player, multiplier);
				this.core.debug(String.format("Saved Sell Multiplier of player %s", player.getName()), this);
			});
		} else {
			this.multipliersService.setSellMultiplier(player, multiplier);
			this.core.debug(String.format("Saved Sell Multiplier of player %s", player.getName()), this);
		}
	}

	private void saveTokenMultiplier(Player player, boolean async) {

		PlayerMultiplierBase multiplier = this.tokenMultipliers.remove(player.getUniqueId());

		if (async) {
			Schedulers.async().run(() -> {
				this.multipliersService.setTokenMultiplier(player, multiplier);
				this.core.debug(String.format("Saved Token Multiplier of player %s", player.getName()), this);
			});
		} else {
			this.multipliersService.setTokenMultiplier(player, multiplier);
			this.core.debug(String.format("Saved Token Multiplier of player %s", player.getName()), this);
		}
	}

	private void loadSellMultiplier(Player player) {
		Schedulers.async().run(() -> {

			PlayerMultiplierBase multiplier = this.multipliersService.getSellMultiplier(player);

			if (multiplier == null) {
				return;
			}

			this.sellMultipliers.put(player.getUniqueId(), multiplier);

			this.core.debug(String.format("Loaded Sell Multiplier %.2fx for player %s", multiplier.getMultiplier(), player.getName()), this);
		});
	}

	private void loadTokenMultiplier(Player player) {
		Schedulers.async().run(() -> {

			PlayerMultiplierBase multiplier = this.multipliersService.getTokenMultiplier(player);

			if (multiplier == null) {
				return;
			}

			this.tokenMultipliers.put(player.getUniqueId(), multiplier);

			this.core.debug(String.format("Loaded Token Multiplier %.2fx for player %s", multiplier.getMultiplier(), player.getName()), this);
		});
	}

	private void removeExpiredMultipliers() {
		Schedulers.async().run(() -> {
			this.multipliersService.removeExpiredMultipliers();
			this.core.debug("Removed expired multipliers from DB.", this);
		});
	}


	@Override
	public void disable() {
		super.disable();
		this.saveAllMultipliers();
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public void resetPlayerData() {
		this.multipliersRepository.clearTableData();
	}

	private void saveAllMultipliers() {
		Players.all().forEach(p -> {
			saveSellMultiplier(p, false);
			saveTokenMultiplier(p, false);
		});
		info("&aMultipliers saved.");
	}


	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(getConfig().get().getString("messages." + key)));
		}
	}

	private void registerCommands() {
		Commands.create()
				.assertPermission("xprison.multipliers.admin")
				.handler(c -> {
					if (c.args().size() == 4) {
						Player onlinePlayer = c.arg(0).parseOrFail(Player.class);
						double amount = c.arg(1).parseOrFail(Double.class);
						int duration = c.arg(2).parseOrFail(Integer.class);

						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), "&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ","));
							return;
						}

						setupSellMultiplier(c.sender(), onlinePlayer, amount, timeUnit, duration);
					} else if (c.args().size() == 2 && c.rawArg(1).equalsIgnoreCase("reset")) {
						Player onlinePlayer = Players.getNullable(c.rawArg(0));
						resetSellMultiplier(c.sender(), onlinePlayer);
					} else {
						PlayerUtils.sendMessage(c.sender(), "&cInvalid usage!");
						PlayerUtils.sendMessage(c.sender(), "&c/sellmulti <player> <multiplier> <time> <time_unit>");
						PlayerUtils.sendMessage(c.sender(), "&c/sellmulti <player> reset");
					}
				}).registerAndBind(this, "sellmulti", "sellmultiplier", "smulti");
		Commands.create()
				.assertPermission("xprison.multipliers.admin")
				.handler(c -> {
					if (c.args().size() == 4) {
						Player onlinePlayer = c.arg(0).parseOrFail(Player.class);
						double amount = c.arg(1).parseOrFail(Double.class);
						int duration = c.arg(2).parseOrFail(Integer.class);
						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), "&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ","));
							return;
						}
						setupTokenMultiplier(c.sender(), onlinePlayer, amount, timeUnit, duration);
					} else if (c.args().size() == 2 && c.rawArg(1).equalsIgnoreCase("reset")) {
						Player onlinePlayer = Players.getNullable(c.rawArg(0));
						resetTokenMultiplier(c.sender(), onlinePlayer);
					} else {
						PlayerUtils.sendMessage(c.sender(), "&cInvalid usage!");
						PlayerUtils.sendMessage(c.sender(), "&c/tokenmulti <player> <multiplier> <time> <time_unit>");
						PlayerUtils.sendMessage(c.sender(), "&c/tokenmulti <player> reset");
					}
				}).registerAndBind(this, "tokenmulti", "tokenmultiplier", "tmulti");
		Commands.create()
				.assertPlayer()
				.handler(c -> {

					PlayerMultiplierBase sellMulti = this.getSellMultiplier(c.sender());
					PlayerMultiplierBase tokenMulti = this.getTokenMultiplier(c.sender());

					double sellMultiplier = sellMulti == null || !sellMulti.isValid() ? 0.0 : sellMulti.getMultiplier();
					double tokenMultiplier = tokenMulti == null || !tokenMulti.isValid() ? 0.0 : tokenMulti.getMultiplier();
					String sellMultiplierDuration = sellMulti == null || sellMulti.isExpired() ? "" : sellMulti.getTimeLeftString();
					String tokenMultiplierDuration = tokenMulti == null || tokenMulti.isExpired() ? "" : tokenMulti.getTimeLeftString();

					PlayerUtils.sendMessage(c.sender(), messages.get("sell_multi").replace("%multiplier%", String.format("%,.2f", sellMultiplier)).replace("%duration%", sellMultiplierDuration));
					PlayerUtils.sendMessage(c.sender(), messages.get("token_multi").replace("%multiplier%", String.format("%,.2f", tokenMultiplier)).replace("%duration%", tokenMultiplierDuration));
				}).registerAndBind(this, "multiplier", "multi");
	}

	private void resetSellMultiplier(CommandSender sender, Player onlinePlayer) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender,"&cPlayer must be online!");
			return;
		}

		if (this.sellMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			this.sellMultipliers.remove(onlinePlayer.getUniqueId());
			PlayerUtils.sendMessage(sender, String.format("&aYou have reset &e%s's &eSell Multiplier.", onlinePlayer.getName()));
			PlayerUtils.sendMessage(onlinePlayer, messages.get("sell_multi_reset"));
		} else {
			PlayerUtils.sendMessage(sender, String.format("&cCould not fetch the &e%s's &eSell Multiplier.", onlinePlayer.getName()));
		}
	}

	private void resetTokenMultiplier(CommandSender sender, Player onlinePlayer) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, "&cPlayer must be online!");
			return;
		}

		if (this.tokenMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			this.tokenMultipliers.remove(onlinePlayer.getUniqueId());
			PlayerUtils.sendMessage(sender, String.format("&aYou have reset &e%s's &eToken Multiplier.", onlinePlayer.getName()));
			PlayerUtils.sendMessage(onlinePlayer, messages.get("token_multi_reset"));
		} else {
			PlayerUtils.sendMessage(sender, String.format("&cCould not fetch the &e%s's &eToken Multiplier.", onlinePlayer.getName()));
		}
	}

	private void setupSellMultiplier(CommandSender sender, Player onlinePlayer, double amount, TimeUnit timeUnit, int duration) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, "&cPlayer must be online!");
			return;
		}

		if (sellMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			PlayerMultiplierBase multiplier = sellMultipliers.get(onlinePlayer.getUniqueId());

			if (multiplier.isExpired()) {
				multiplier.reset();
			}

			double finalMulti = multiplier.getMultiplier() + amount > this.playerSellMultiMax ? this.playerSellMultiMax : multiplier.getMultiplier() + amount;

			multiplier.setMultiplier(finalMulti);
			multiplier.addDuration(timeUnit, duration);

			sellMultipliers.put(onlinePlayer.getUniqueId(), multiplier);
		} else {
			sellMultipliers.put(onlinePlayer.getUniqueId(), new PlayerMultiplierBase(onlinePlayer.getUniqueId(), Math.min(amount, this.playerSellMultiMax), timeUnit, duration, MultiplierType.SELL));
		}
		PlayerUtils.sendMessage(onlinePlayer, messages.get("sell_multi_apply").replace("%multiplier%", String.valueOf(amount)).replace("%time%", duration + " " + StringUtils.capitalize(timeUnit.name())));
		PlayerUtils.sendMessage(sender, String.format("&aYou have set &e%s's &eSell Multiplier &ato &e%.2f &afor &e%d &a%s.", onlinePlayer.getName(), amount, duration, StringUtils.capitalize(timeUnit.name())));
	}

	private void setupTokenMultiplier(CommandSender sender, Player onlinePlayer, double amount, TimeUnit timeUnit, int duration) {
		if (onlinePlayer == null || !onlinePlayer.isOnline()) {
			PlayerUtils.sendMessage(sender, "&cPlayer must be online!");
			return;
		}

		if (tokenMultipliers.containsKey(onlinePlayer.getUniqueId())) {
			PlayerMultiplierBase multiplier = tokenMultipliers.get(onlinePlayer.getUniqueId());

			if (multiplier.isExpired()) {
				multiplier.reset();
			}

			double finalMulti = multiplier.getMultiplier() + amount > this.playerTokenMultiMax ? this.playerTokenMultiMax : multiplier.getMultiplier() + amount;

			multiplier.setMultiplier(finalMulti);
			multiplier.addDuration(timeUnit, duration);

			tokenMultipliers.put(onlinePlayer.getUniqueId(), multiplier);
		} else {
			tokenMultipliers.put(onlinePlayer.getUniqueId(), new PlayerMultiplierBase(onlinePlayer.getUniqueId(), Math.min(amount, this.playerTokenMultiMax), timeUnit, duration, MultiplierType.TOKENS));
		}

		PlayerUtils.sendMessage(onlinePlayer, messages.get("token_multi_apply").replace("%multiplier%", String.valueOf(amount)).replace("%time%", duration + " " + StringUtils.capitalize(timeUnit.name())));
		PlayerUtils.sendMessage(sender, String.format("&aYou have set &e%s's &eToken Multiplier &ato &e%.2f &afor &e%d &a%s.", onlinePlayer.getName(), amount, duration, StringUtils.capitalize(timeUnit.name())));
	}


	public PlayerMultiplierBase getSellMultiplier(Player p) {
		return sellMultipliers.get(p.getUniqueId());
	}

	public PlayerMultiplierBase getTokenMultiplier(Player p) {
		return tokenMultipliers.get(p.getUniqueId());
	}

	private PlayerMultiplierBase calculateRankMultiplier(Player p) {
		PlayerMultiplierBase toReturn = new PlayerMultiplierBase(p.getUniqueId(), 0.0, 0, MultiplierType.SELL);

		for (String perm : this.permissionToMultiplier.keySet()) {
			if (p.hasPermission(perm)) {
				toReturn.addMultiplier(this.permissionToMultiplier.get(perm));
				break;
			}
		}

		return toReturn;
	}


}
