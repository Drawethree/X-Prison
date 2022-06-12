package dev.drawethree.ultraprisoncore.autominer.manager;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutoMinerTimeReceiveEvent;
import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutomineEvent;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AutoMinerManager {

	private final UltraPrisonAutoMiner plugin;

	private final Map<UUID, Integer> autoMinerTimes;

	public AutoMinerManager(UltraPrisonAutoMiner plugin) {
		this.plugin = plugin;
		this.autoMinerTimes = new HashMap<>();
	}

	public void loadAllPlayersAutoMinerData() {
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

		PlayerUtils.sendMessage(sender, this.plugin.getMessage("auto_miner_time_add").replace("%time%", String.valueOf(time)).replace("%timeunit%", unit.name()).replace("%player%", p.getName()));
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

	public String getPlayerAutoMinerTimeLeftFormatted(Player p) {

		int timeLeft = this.getAutoMinerTime(p);

		if (timeLeft == 0) {
			return "0s";
		}

		long days = timeLeft / (24 * 60 * 60);
		timeLeft -= days * (24 * 60 * 60);

		long hours = timeLeft / (60 * 60);
		timeLeft -= hours * (60 * 60);

		long minutes = timeLeft / (60);
		timeLeft -= minutes * (60);

		long seconds = timeLeft;

		return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
	}

	public int getAutoMinerTime(Player player) {
		return this.autoMinerTimes.getOrDefault(player.getUniqueId(), 0);
	}

	public boolean isInAutoMinerRegion(Player player) {
		if (this.plugin.getRegion() == null) {
			return false;
		}
		return this.plugin.getRegion().getRegion().contains(player.getLocation());
	}

	public PlayerAutomineEvent callAutoMineEvent(Player p) {
		PlayerAutomineEvent event = new PlayerAutomineEvent(p, this.getAutoMinerTime(p));
		Events.callSync(event);
		return event;
	}

	public void saveAllPlayerAutoMinerData(boolean async) {
		Players.all().forEach(p -> savePlayerAutoMinerData(p, async));
	}
}
