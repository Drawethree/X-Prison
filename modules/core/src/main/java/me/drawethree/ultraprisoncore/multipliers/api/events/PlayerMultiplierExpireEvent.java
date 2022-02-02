package me.drawethree.ultraprisoncore.multipliers.api.events;

import lombok.Getter;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerMultiplierExpireEvent extends UltraPrisonPlayerEvent {


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
