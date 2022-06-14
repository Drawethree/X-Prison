package dev.drawethree.ultraprisoncore.autominer.manager;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutoMinerTimeReceiveEvent;
import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutomineEvent;
import dev.drawethree.ultraprisoncore.autominer.model.AutoMinerRegion;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import lombok.Getter;
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
import java.util.concurrent.TimeUnit;

public class AutoMinerManager {

	private final UltraPrisonAutoMiner plugin;

	private final Map<UUID, Integer> autoMinerTimes;

	@Getter
	private AutoMinerRegion autoMinerRegion;

	public AutoMinerManager(UltraPrisonAutoMiner plugin) {
		this.plugin = plugin;
		this.autoMinerTimes = new HashMap<>();
	}

	private void loadAllPlayersAutoMinerData() {
		Players.all().forEach(this::loadPlayerAutoMinerData);
	}

	public void loadPlayerAutoMinerData(Player p) {
		Schedulers.async().run(() -> {
			int timeLeft = this.plugin.getCore().getPluginDatabase().getPlayerAutoMinerTime(p);
			this.autoMinerTimes.put(p.getUniqueId(), timeLeft);
			this.plugin.getCore().getLogger().info(String.format("Loaded %s's AutoMiner Time.", p.getName()));
		});
	}

	public void savePlayerAutoMinerData(Player p, boolean async) {

		int timeLeft = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);

		if (async) {
			Schedulers.async().run(() -> savePlayerAutominerData(p, timeLeft));
		} else {
			savePlayerAutominerData(p, timeLeft);
		}
	}

	private void savePlayerAutominerData(Player p, int timeLeft) {
		this.plugin.getCore().getPluginDatabase().saveAutoMiner(p, timeLeft);
		this.autoMinerTimes.remove(p.getUniqueId());
		this.plugin.getCore().getLogger().info(String.format("Saved %s's AutoMiner time.", p.getName()));
	}

	public void givePlayerAutoMinerTime(CommandSender sender, Player p, long time, TimeUnit unit) {

		if (p == null || !p.isOnline()) {
			PlayerUtils.sendMessage(sender, "&cPlayer is not online!");
			return;
		}

		int currentTime = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
		currentTime += unit.toSeconds(time);

		autoMinerTimes.put(p.getUniqueId(), currentTime);

		this.callAutoMinerTimeReceiveEvent(p, time, unit);

		PlayerUtils.sendMessage(sender, this.plugin.getAutoMinerConfig().getMessage("auto_miner_time_add").replace("%time%", String.valueOf(time)).replace("%timeunit%", unit.name()).replace("%player%", p.getName()));
	}

	private PlayerAutoMinerTimeReceiveEvent callAutoMinerTimeReceiveEvent(Player p, long time, TimeUnit unit) {
		PlayerAutoMinerTimeReceiveEvent event = new PlayerAutoMinerTimeReceiveEvent(p, unit, time);
		Events.callSync(event);
		return event;
	}

	public boolean hasAutoMinerTime(Player p) {
		return autoMinerTimes.getOrDefault(p.getUniqueId(), 0) > 0;
	}

	public void decrementPlayerAutominerTime(Player p) {
		int newAmount = autoMinerTimes.get(p.getUniqueId()) - 1;
		autoMinerTimes.put(p.getUniqueId(), newAmount);
	}

	public int getAutoMinerTime(Player player) {
		return this.autoMinerTimes.getOrDefault(player.getUniqueId(), 0);
	}

	public boolean isInAutoMinerRegion(Player player) {
		if (this.autoMinerRegion == null) {
			return false;
		}
		return this.autoMinerRegion.getRegion().contains(player.getLocation());
	}

	public PlayerAutomineEvent callAutoMineEvent(Player p) {
		PlayerAutomineEvent event = new PlayerAutomineEvent(p, this.getAutoMinerTime(p));
		Events.callSync(event);
		return event;
	}

	public void saveAllPlayerAutoMinerData(boolean async) {
		Players.all().forEach(p -> savePlayerAutoMinerData(p, async));
	}

	private void loadAutoMinerRegion() {

		YamlConfiguration configuration = this.plugin.getAutoMinerConfig().getYamlConfig();

		String worldName = configuration.getString("auto-miner-region.world");
		World world = Bukkit.getWorld(worldName);

		if (world == null) {
			plugin.getCore().getLogger().warning(String.format("Unable to get world with name %s!  Disabling AutoMiner region.", worldName));
			return;
		}

		int rewardPeriod = configuration.getInt("auto-miner-region.reward-period");

		if (rewardPeriod <= 0) {
			plugin.getCore().getLogger().warning("reward-perion in autominer.yml needs to be greater than 0!  Disabling AutoMiner region.");
			return;
		}

		String regionName = configuration.getString("auto-miner-region.name");
		Optional<IWrappedRegion> optRegion = WorldGuardWrapper.getInstance().getRegion(world, regionName);

		if (optRegion.isEmpty()) {
			plugin.getCore().getLogger().warning(String.format("There is no such region named %s in world %s!  Disabling AutoMiner region.", regionName, world.getName()));
			return;
		}

		List<String> rewards = configuration.getStringList("auto-miner-region.rewards");

		if (rewards.isEmpty()) {
			plugin.getCore().getLogger().warning("rewards in autominer.yml are empty! Disabling AutoMiner region.");
			return;
		}

		int blocksBroken = configuration.getInt("auto-miner-region.blocks-broken");

		if (blocksBroken <= 0) {
			this.plugin.getCore().getLogger().warning("blocks-broken in autominer.yml needs to be greater than 0!  Disabling AutoMiner region.");
			return;
		}

		this.autoMinerRegion = new AutoMinerRegion(this.plugin, world, optRegion.get(), rewards, rewardPeriod, blocksBroken);
		this.autoMinerRegion.startAutoMinerTask();

		this.plugin.getCore().getLogger().info("AutoMiner region loaded successfully!");
	}

	public void load() {
		this.removeExpiredAutoMiners();
		this.loadAllPlayersAutoMinerData();
		this.loadAutoMinerRegion();
	}

	private void removeExpiredAutoMiners() {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getPluginDatabase().removeExpiredAutoMiners();
			this.plugin.getCore().getLogger().info("Removed expired AutoMiners from database");
		});
	}

	public void reload() {
		this.stopAutoMinerTask();
		this.loadAutoMinerRegion();
	}

	public void disable() {
		this.stopAutoMinerTask();
		this.saveAllPlayerAutoMinerData(false);
	}

	private void stopAutoMinerTask() {
		if (this.autoMinerRegion != null) {
			this.autoMinerRegion.stopAutoMinerTask();
		}
	}
}
