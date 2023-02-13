package dev.drawethree.xprison.autominer.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class PlayerAutoMinerTimeModifyEvent extends XPrisonPlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	@Setter
	private final TimeUnit timeUnit;

	@Getter
	@Setter
	private final long duration;

	/**
	 * Called when player received autominer time
	 *
	 * @param player   Player
	 * @param unit     TimeUnit
	 * @param duration duration, can be negative
	 */
	public PlayerAutoMinerTimeModifyEvent(Player player, TimeUnit unit, long duration) {
		super(player);
		this.timeUnit = unit;
		this.duration = duration;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

}
