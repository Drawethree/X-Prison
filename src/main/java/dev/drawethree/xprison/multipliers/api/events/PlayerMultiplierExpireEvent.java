package dev.drawethree.xprison.multipliers.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplier;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated
public final class PlayerMultiplierExpireEvent extends XPrisonPlayerEvent {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final PlayerMultiplier multiplier;

	/**
	 * Called when player's multiplier expires
	 *
	 * @param player     Player
	 * @param multiplier multiplier
	 */
	public PlayerMultiplierExpireEvent(Player player, PlayerMultiplier multiplier) {
		super(player);
		this.multiplier = multiplier;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

}
