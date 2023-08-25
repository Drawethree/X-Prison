package dev.drawethree.xprison.tokens.api.events;

import dev.drawethree.xprison.api.enums.LostCause;
import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlayerTokensLostEvent extends XPrisonPlayerEvent {


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
	public PlayerTokensLostEvent(LostCause cause, OfflinePlayer player, long amount) {
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
