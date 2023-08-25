package dev.drawethree.xprison.autominer.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlayerAutomineEvent extends XPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	@Setter
	private boolean cancelled;

	@Getter
	private final int timeLeft;

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
