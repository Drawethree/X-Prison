package me.drawethree.ultraprisoncore.multipliers.api.events;

import lombok.Getter;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PlayerMultiplierReceiveEvent extends UltraPrisonPlayerEvent {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final double multiplier;

	@Getter
	private final TimeUnit timeUnit;

	@Getter
	private final long duration;

	@Getter
	private final MultiplierType type;

	/**
	 * Called when player receive multiplier
	 *
	 * @param player     Player
	 * @param multiplier multiplier
	 * @param duration   duration
	 * @param timeUnit   TimeUnit
	 */
	public PlayerMultiplierReceiveEvent(Player player, double multiplier, TimeUnit timeUnit, long duration, MultiplierType type) {
		super(player);
		this.multiplier = multiplier;
		this.timeUnit = timeUnit;
		this.duration = duration;
		this.type = type;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
