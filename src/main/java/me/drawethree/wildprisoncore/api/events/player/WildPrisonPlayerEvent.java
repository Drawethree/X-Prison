package me.drawethree.wildprisoncore.api.events.player;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;


public abstract class WildPrisonPlayerEvent extends Event {

	@Getter
	protected OfflinePlayer player;

	public WildPrisonPlayerEvent(OfflinePlayer player) {
		this.player = player;
	}
}
