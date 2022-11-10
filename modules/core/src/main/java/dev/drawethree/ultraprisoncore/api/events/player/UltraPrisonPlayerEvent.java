package dev.drawethree.ultraprisoncore.api.events.player;

import dev.drawethree.ultraprisoncore.api.events.UltraPrisonEvent;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

public abstract class UltraPrisonPlayerEvent extends UltraPrisonEvent {

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
