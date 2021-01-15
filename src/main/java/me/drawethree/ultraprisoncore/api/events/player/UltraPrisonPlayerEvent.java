package me.drawethree.ultraprisoncore.api.events.player;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;


public abstract class UltraPrisonPlayerEvent extends Event {

	@Getter
	protected OfflinePlayer player;

	/**
	 * Abstract UltraPrisonPlayerEvent
	 *
	 * @param player Player
	 */
	public UltraPrisonPlayerEvent(OfflinePlayer player) {
		this.player = player;
	}
}
