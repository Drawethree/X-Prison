package dev.drawethree.xprison.autominer.manager;

import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.autominer.api.events.PlayerAutoMinerTimeModifyEvent;
import dev.drawethree.xprison.autominer.api.events.PlayerAutomineEvent;
import dev.drawethree.xprison.autominer.model.AutoMinerRegion;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AutoMinerManager {

	private final XPrisonAutoMiner plugin;

	private final Map<UUID, Integer> autoMinerTimes;

	private List<AutoMinerRegion> autoMinerRegions;

	public AutoMinerManager(XPrisonAutoMiner plugin) {
		this.plugin = plugin;
		this.autoMinerTimes = new ConcurrentHashMap<>();
	}

	private void loadAllPlayersAutoMinerData() {
		Schedulers.async().run(() -> {
			for (Player p : Players.all()) {
				int timeLeft = this.plugin.getAutominerService().getPlayerAutoMinerTime(p);
				this.autoMinerTimes.put(p.getUniqueId(), timeLeft);
				this.plugin.getCore().debug(String.format("Loaded %s's AutoMiner Time.", p.getName()), this.plugin);
			}
		});
	}


	public void loadPlayerAutoMinerData(Player p) {
		Schedulers.async().run(() -> {
			int timeLeft = this.plugin.getAutominerService().getPlayerAutoMinerTime(p);
			this.autoMinerTimes.put(p.getUniqueId(), timeLeft);
			this.plugin.getCore().debug(String.format("Loaded %s's AutoMiner Time.", p.getName()), this.plugin);
		});
	}

	public void savePlayerAutoMinerData(Player p, boolean async) {

		int timeLeft = getAutoMinerTime(p);

		if (async) {
			Schedulers.async().run(() -> savePlayerAutominerData(p, timeLeft));
		} else {
			savePlayerAutominerData(p, timeLeft);
		}
	}

	private void savePlayerAutominerData(Player p, int timeLeft) {
		this.plugin.getAutominerService().setAutoMiner(p, timeLeft);
		this.autoMinerTimes.remove(p.getUniqueId());
		this.plugin.getCore().debug(String.format("Saved %s's AutoMiner time.", p.getName()), this.plugin);
	}

	public void modifyPlayerAutoMinerTime(CommandSender sender, Player p, long time, TimeUnit unit) {

		if (p == null || !p.isOnline()) {
			PlayerUtils.sendMessage(sender, "&cPlayer is not online!");
			return;
		}

		PlayerAutoMinerTimeModifyEvent event = this.callAutoMinerTimeModifyEvent(p, time, unit);

		time = event.getDuration();
		unit = event.getTimeUnit();

		int currentTime = getAutoMinerTime(p);
		currentTime += unit.toSeconds(time);

		if (currentTime < 0) {
			currentTime = 0;
		}

		this.autoMinerTimes.put(p.getUniqueId(), currentTime);

		String messageKey = time < 0 ? "auto_miner_time_remove" : "auto_miner_time_add";
		PlayerUtils.sendMessage(sender, this.plugin.getAutoMinerConfig().getMessage(messageKey).replace("%time%", String.valueOf(Math.abs(time))).replace("%timeunit%", unit.name()).replace("%player%", p.getName()));
	}

	private PlayerAutoMinerTimeModifyEvent callAutoMinerTimeModifyEvent(Player p, long time, TimeUnit unit) {
		PlayerAutoMinerTimeModifyEvent event = new PlayerAutoMinerTimeModifyEvent(p, unit, time);
		Events.callSync(event);
		return event;
	}

	public boolean hasAutoMinerTime(Player p) {
		return getAutoMinerTime(p) > 0;
	}

	public void decrementPlayerAutominerTime(Player p) {
		int newAmount = autoMinerTimes.get(p.getUniqueId()) - 1;
		autoMinerTimes.put(p.getUniqueId(), newAmount);
	}

	public int getAutoMinerTime(Player player) {
		return this.autoMinerTimes.getOrDefault(player.getUniqueId(), 0);
	}

	public boolean isInAutoMinerRegion(Player player) {
		for (AutoMinerRegion region : this.autoMinerRegions) {
			if (region.getRegion().contains(player.getLocation())) {
				return true;
			}
		}
		return false;
	}

	public PlayerAutomineEvent callAutoMineEvent(Player p) {
		PlayerAutomineEvent event = new PlayerAutomineEvent(p, this.getAutoMinerTime(p));
		Events.callSync(event);
		return event;
	}

	public void saveAllPlayerAutoMinerData(boolean async) {
		Players.all().forEach(p -> savePlayerAutoMinerData(p, async));
		this.plugin.getCore().getLogger().info("Saved online players auto miner data.");
	}

	private void loadAutoMinerRegions() {
		this.autoMinerRegions = new ArrayList<>();

		YamlConfiguration configuration = this.plugin.getAutoMinerConfig().getYamlConfig();

		Set<String> regionNames = configuration.getConfigurationSection("auto-miner-regions").getKeys(false);

		for (String regionName : regionNames) {
			String worldName = configuration.getString("auto-miner-regions." + regionName + ".world");
			World world = Bukkit.getWorld(worldName);

			if (world == null) {
				plugin.getCore().getLogger().warning(String.format("Unable to get world with name %s!  Disabling AutoMiner region.", worldName));
				return;
			}

			int rewardPeriod = configuration.getInt("auto-miner-regions." + regionName + ".reward-period");

			if (rewardPeriod <= 0) {
				plugin.getCore().getLogger().warning("reward-period in autominer.yml for region " + regionName + " needs to be greater than 0!");
				return;
			}

			Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(world, regionName);

			if (!optRegion.isPresent()) {
				plugin.getCore().getLogger().warning(String.format("There is no such region named %s in world %s!", regionName, world.getName()));
				return;
			}

			List<String> rewards = configuration.getStringList("auto-miner-regions." + regionName + ".rewards");

			if (rewards.isEmpty()) {
				plugin.getCore().getLogger().warning("rewards in autominer.yml for region " + regionName + " are empty!");
				return;
			}

			int blocksBroken = configuration.getInt("auto-miner-regions." + regionName + ".blocks-broken");

			if (blocksBroken <= 0) {
				this.plugin.getCore().getLogger().warning("blocks-broken in autominer.yml for region " + regionName + " needs to be greater than 0!");
				return;
			}

			AutoMinerRegion region = new AutoMinerRegion(this.plugin, world, optRegion.get(), rewards, rewardPeriod, blocksBroken);
			region.startAutoMinerTask();

			this.plugin.getCore().getLogger().info("AutoMiner region '" + regionName + "' loaded successfully!");
			this.autoMinerRegions.add(region);
		}
	}

	public void load() {
		this.removeExpiredAutoMiners();
		this.loadAllPlayersAutoMinerData();
		this.loadAutoMinerRegions();
	}

	private void removeExpiredAutoMiners() {
		Schedulers.async().run(() -> {
			this.plugin.getAutominerService().removeExpiredAutoMiners();
			this.plugin.getCore().debug("Removed expired AutoMiners from database", this.plugin);
		});
	}

	public void reload() {
		this.stopAutoMinerRegions();
		this.loadAutoMinerRegions();
	}

	public void disable() {
		this.stopAutoMinerRegions();
		this.saveAllPlayerAutoMinerData(false);
	}

	private void stopAutoMinerRegions() {
		for (AutoMinerRegion region : this.autoMinerRegions) {
			region.stopAutoMinerTask();
		}
	}
}
