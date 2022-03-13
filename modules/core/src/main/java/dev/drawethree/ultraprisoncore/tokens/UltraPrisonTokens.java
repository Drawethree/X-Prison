package dev.drawethree.ultraprisoncore.tokens;


import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import dev.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPI;
import dev.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPIImpl;
import dev.drawethree.ultraprisoncore.tokens.commands.TokensCommand;
import dev.drawethree.ultraprisoncore.tokens.managers.CommandManager;
import dev.drawethree.ultraprisoncore.tokens.managers.TokensManager;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.reflect.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class UltraPrisonTokens implements UltraPrisonModule {

	public static final String TABLE_NAME_TOKENS = "UltraPrison_Tokens";
	public static final String TABLE_NAME_BLOCKS = "UltraPrison_BlocksBroken";
	public static final String TABLE_NAME_BLOCKS_WEEKLY = "UltraPrison_BlocksBrokenWeekly";
	public static final String MODULE_NAME = "Tokens";
	public static final String TOKENS_ADMIN_PERM = "ultraprison.tokens.admin";

	@Getter
	private static UltraPrisonTokens instance;

	@Getter
	private FileManager.Config config;

	@Getter
	private FileManager.Config blockRewardsConfig;

	@Getter
	private UltraPrisonTokensAPI api;

	@Getter
	private TokensManager tokensManager;

	@Getter
	private CommandManager commandManager;

	@Getter
	private final UltraPrisonCore core;

	private Map<String, String> messages;
	private Map<Material, List<String>> luckyBlockRewards;


	private double chance;
	private long minAmount;
	private long maxAmount;
	private boolean enabled;
	@Getter
	private long commandCooldown;


	public UltraPrisonTokens(UltraPrisonCore prisonCore) {
		instance = this;
		this.core = prisonCore;
	}


	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {

		this.config.reload();
		this.blockRewardsConfig.reload();

		this.loadMessages();
		this.loadVariables();
		this.tokensManager.reloadConfig();
		this.commandManager.reload();
	}

	private void loadVariables() {
		this.chance = getConfig().get().getDouble("tokens.breaking.chance");
		this.minAmount = getConfig().get().getLong("tokens.breaking.min");
		this.maxAmount = getConfig().get().getLong("tokens.breaking.max");

		this.commandCooldown = getConfig().get().getLong("tokens-command-cooldown");

		this.luckyBlockRewards = new HashMap<>();

		for (String key : this.getConfig().get().getConfigurationSection("lucky-blocks").getKeys(false)) {
			CompMaterial material = CompMaterial.fromString(key);
			List<String> rewards = this.getConfig().get().getStringList("lucky-blocks." + key);
			if (rewards.isEmpty()) {
				continue;
			}
			this.luckyBlockRewards.put(material.toMaterial(), rewards);
		}

	}


	@Override
	public void enable() {

		this.enabled = true;
		this.config = this.core.getFileManager().getConfig("tokens.yml").copyDefaults(true).save();
		this.blockRewardsConfig = this.core.getFileManager().getConfig("block-rewards.yml").copyDefaults(true).save();

		this.loadMessages();
		this.loadVariables();

		this.tokensManager = new TokensManager(this);
		this.commandManager = new CommandManager(this);
		this.api = new UltraPrisonTokensAPIImpl(this.tokensManager);

		this.commandManager.registerCommands();

		this.registerEvents();
	}


	@Override
	public void disable() {
		this.tokensManager.stopUpdating();
		this.tokensManager.saveWeeklyReset();
		this.tokensManager.savePlayerDataOnDisable();
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME_BLOCKS, TABLE_NAME_TOKENS, TABLE_NAME_BLOCKS_WEEKLY};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case MYSQL:
			case SQLITE: {
				return new String[]{
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TOKENS + "(UUID varchar(36) NOT NULL UNIQUE, Tokens bigint, primary key (UUID))",
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BLOCKS + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))",
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BLOCKS_WEEKLY + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))"
				};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	private void registerEvents() {

		Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
				.filter(e -> e.getItem() != null && e.getItem().getType() == this.tokensManager.getTokenItemMaterial() && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
				.handler(e -> {
					if (e.getItem().hasItemMeta()) {
						e.setCancelled(true);
						e.setUseInteractedBlock(Event.Result.DENY);
						boolean offHandClick = false;
						if (MinecraftVersion.getRuntimeVersion().isAfter(MinecraftVersion.of(1, 8, 9))) {
							offHandClick = e.getHand() == EquipmentSlot.OFF_HAND;
						}
						this.tokensManager.redeemTokens(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking(), offHandClick);
					}
				}).bindWith(core);

		Events.subscribe(BlockBreakEvent.class)
				.filter(EventFilters.ignoreCancelled())
				.filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch(region -> region.getId().toLowerCase().startsWith("mine")))
				.filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && this.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.handler(e -> {
					List<Block> blocks = new ArrayList<>(1);
					blocks.add(e.getBlock());
					this.handleBlockBreak(e.getPlayer(), blocks, true);
				}).bindWith(core);
	}

	public void handleBlockBreak(Player p, List<Block> blocks, boolean countBlocksBroken) {
		long startTime = System.currentTimeMillis();
		//Remove AIR blocks.
		blocks.removeIf(block -> block.getType() == Material.AIR);

		if (countBlocksBroken) {
			tokensManager.addBlocksBroken(p, blocks);
		}

		boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(p.getPlayer());

		//Lucky block check
		blocks.stream().filter(block -> luckyBlockRewards.containsKey(block.getType())).collect(Collectors.toList()).forEach(block -> {
			List<String> rewards = this.luckyBlockRewards.get(block.getType());
			for (String s : rewards) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()));
			}
		});

		long totalAmount = 0;
		for (int i = 0; i < blocks.size(); i++) {
			double random = ThreadLocalRandom.current().nextDouble(100);

			if (this.chance >= random) {
				long randAmount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
				randAmount = luckyBooster ? randAmount * 2 : randAmount;
				totalAmount += randAmount;
			}
		}
		if (totalAmount > 0) {
			tokensManager.giveTokens(p, totalAmount, null, ReceiveCause.MINING);
		}
		this.getCore().debug("UltraPrisonTokens::handleBlockBreak >> Took " + (System.currentTimeMillis() - startTime) + " ms.", this);
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key, TextUtils.applyColor(this.getConfig().get().getString("messages." + key)));
		}
	}

	public String getMessage(String key) {
		return messages.get(key);
	}
}
