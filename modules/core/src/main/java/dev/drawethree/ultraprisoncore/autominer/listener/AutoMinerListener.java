package dev.drawethree.ultraprisoncore.autominer.listener;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import me.lucko.helper.Events;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AutoMinerListener {

	private final UltraPrisonAutoMiner plugin;

	public AutoMinerListener(UltraPrisonAutoMiner plugin) {
		this.plugin = plugin;
	}

	public void subscribeToEvents() {
		this.subscribeToPlayerJoinEvent();
		this.subscribeToPlayerQuitEvent();
	}

	private void subscribeToPlayerQuitEvent() {
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> this.plugin.getManager().savePlayerAutoMinerData(e.getPlayer(), true)).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerJoinEvent() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> this.plugin.getManager().loadPlayerAutoMinerData(e.getPlayer())).bindWith(this.plugin.getCore());
	}

}
