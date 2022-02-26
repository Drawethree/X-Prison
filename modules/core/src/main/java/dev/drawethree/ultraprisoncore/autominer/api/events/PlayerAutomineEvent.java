package dev.drawethree.ultraprisoncore.autominer.api.events;

import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerAutomineEvent extends UltraPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	@Setter
	private boolean cancelled;

	@Getter
	private int timeLeft;

	/**
	 * Called when player auto mines in region
	 *
	 * @param player   Player
	 * @param timeLeft Timeleft in seconds of player's autominer time
	 */
	public PlayerAutomineEvent(Player player, int timeLeft) {
		super(player);
		this.timeLeft = timeLeft;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

}
