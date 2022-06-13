package dev.drawethree.ultraprisoncore.autominer;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autominer.api.UltraPrisonAutoMinerAPI;
import dev.drawethree.ultraprisoncore.autominer.api.UltraPrisonAutoMinerAPIImpl;
import dev.drawethree.ultraprisoncore.autominer.manager.AutoMinerManager;
import dev.drawethree.ultraprisoncore.autominer.model.AutoMinerRegion;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class UltraPrisonAutoMiner implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_AutoMiner";
	public static final String MODULE_NAME = "Auto Miner";

	@Getter
	private static UltraPrisonAutoMiner instance;

	@Getter
	private final UltraPrisonCore core;

	@Getter
	private FileManager.Config config;
	private HashMap<String, String> messages;

	@Getter
	private AutoMinerRegion region;

	@Getter
	private UltraPrisonAutoMinerAPI api;

	private boolean enabled;

	@Getter
	private AutoMinerManager manager;

	public UltraPrisonAutoMiner(UltraPrisonCore UltraPrisonCore) {
		this.core = UltraPrisonCore;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {

		if (this.region != null) {
			this.region.stopAutoMinerTask();
		}

		this.config.reload();
		this.loadMessages();

		this.loadAutoMinerRegion();

	}

	@Override
	public void enable() {
		this.enabled = true;
		instance = this;
		this.config = this.core.getFileManager().getConfig("autominer.yml").copyDefaults(true).save();

		this.registerCommands();
		this.registerEvents();
		this.loadMessages();
		this.removeExpiredAutoMiners();

		this.manager = new AutoMinerManager(this);
		this.manager.loadAllPlayersAutoMinerData();

		this.loadAutoMinerRegion();

		this.api = new UltraPrisonAutoMinerAPIImpl(this);
	}

	private void registerEvents() {
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> this.manager.savePlayerAutoMinerData(e.getPlayer(), true)).bindWith(this.core);
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> this.manager.loadPlayerAutoMinerData(e.getPlayer())).bindWith(this.core);
	}

	private void removeExpiredAutoMiners() {
		Schedulers.async().run(() -> {
			this.core.getPluginDatabase().removeExpiredAutoMiners();
			this.core.getLogger().info("Removed expired AutoMiners from database");
		});
	}

	private void loadAutoMinerRegion() {

		String worldName = getConfig().get().getString("auto-miner-region.world");
		World world = Bukkit.getWorld(worldName);

		if (world == null) {
			core.getLogger().warning(String.format("Unable to get world with name %s!  Disabling AutoMiner region.", worldName));
			return;
		}

		int rewardPeriod = getConfig().get().getInt("auto-miner-region.reward-period");

		if (rewardPeriod <= 0) {
			core.getLogger().warning("reward-perion in autominer.yml needs to be greater than 0!  Disabling AutoMiner region.");
			return;
		}

		String regionName = getConfig().get().getString("auto-miner-region.name");
		Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(world, regionName);

		if (!optRegion.isPresent()) {
			core.getLogger().warning(String.format("There is no such region named %s in world %s!  Disabling AutoMiner region.", regionName, world.getName()));
			return;
		}

		List<String> rewards = getConfig().get().getStringList("auto-miner-region.rewards");

		if (rewards.isEmpty()) {
			core.getLogger().warning("rewards in autominer.yml are empty! Disabling AutoMiner region.");
			return;
		}

		int blocksBroken = getConfig().get().getInt("auto-miner-region.blocks-broken");

		if (blocksBroken <= 0) {
			core.getLogger().warning("blocks-broken in autominer.yml needs to be greater than 0!  Disabling AutoMiner region.");
			return;
		}

		this.region = new AutoMinerRegion(this, world, optRegion.get(), rewards, rewardPeriod, blocksBroken);
		this.region.startAutoMinerTask();

		core.getLogger().info("AutoMiner region loaded successfully!");
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key.toLowerCase(), TextUtils.applyColor(getConfig().get().getString("messages." + key)));
		}
	}

	@Override
	public void disable() {
		if (this.manager != null) {
			this.manager.saveAllPlayerAutoMinerData(false);
		}
		if (this.region != null) {
			this.region.stopAutoMinerTask();
		}
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case SQLITE:
			case MYSQL: {
				return new String[]{"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, time int, primary key (UUID))"};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	private void registerCommands() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), messages.get("auto_miner_time").replace("%time%", this.manager.getPlayerAutoMinerTimeLeftFormatted(c.sender())));
					}
				}).registerAndBind(core, "miner", "autominer");
		Commands.create()
				.assertPermission("ultraprison.autominer.admin")
				.handler(c -> {
					if (c.args().size() == 4 && "give".equalsIgnoreCase(c.rawArg(0))) {
						Player target = c.arg(1).parseOrFail(Player.class);
						long time = c.arg(2).parseOrFail(Long.class);

						TimeUnit timeUnit;
						try {
							timeUnit = TimeUnit.valueOf(c.rawArg(3).toUpperCase());
						} catch (IllegalArgumentException e) {
							PlayerUtils.sendMessage(c.sender(), "&cInvalid time unit! Please use one from: " + StringUtils.join(TimeUnit.values(), ","));
							return;
						}

						this.manager.givePlayerAutoMinerTime(c.sender(), target, time, timeUnit);
					}

				}).registerAndBind(core, "adminautominer", "aam");
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), "No message with key '" + key + "' found");
	}
}
