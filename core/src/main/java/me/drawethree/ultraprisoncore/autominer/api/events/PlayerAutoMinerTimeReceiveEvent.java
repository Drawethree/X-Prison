package me.drawethree.ultraprisoncore.autominer.api.events;

import lombok.Getter;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PlayerAutoMinerTimeReceiveEvent extends UltraPrisonPlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final TimeUnit timeUnit;

	@Getter
	private final long duration;

	/**
	 * Called when player auto mines in region
	 *
	 * @param player   Player
	 * @param unit     TimeUnit
	 * @param duration duration
	 */
	public PlayerAutoMinerTimeReceiveEvent(Player player, TimeUnit unit, long duration) {
		super(player);
		this.timeUnit = unit;
		this.duration = duration;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
