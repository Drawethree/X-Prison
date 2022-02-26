package dev.drawethree.ultraprisoncore.gems.api.events;

import dev.drawethree.ultraprisoncore.api.enums.LostCause;
import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerGemsLostEvent extends UltraPrisonPlayerEvent {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final LostCause cause;

	@Getter
	@Setter
	private long amount;

	/**
	 * Called when player loses tokens
	 *
	 * @param cause  LostCause
	 * @param player Player
	 * @param amount Amount of tokens lost
	 */
	public PlayerGemsLostEvent(LostCause cause, OfflinePlayer player, long amount) {
		super(player);
		this.cause = cause;
		this.amount = amount;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

}
