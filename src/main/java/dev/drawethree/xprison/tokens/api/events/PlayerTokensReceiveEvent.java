package dev.drawethree.xprison.tokens.api.events;

import dev.drawethree.xprison.api.enums.ReceiveCause;
import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class PlayerTokensReceiveEvent extends XPrisonPlayerEvent implements Cancellable {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final ReceiveCause cause;

	@Getter
	@Setter
	private long amount;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Called when player receive tokens
	 *
	 * @param cause  ReceiveCause
	 * @param player Player
	 * @param amount Amount of tokens received
	 */
	public PlayerTokensReceiveEvent(ReceiveCause cause, OfflinePlayer player, long amount) {
		super(player);
		this.cause = cause;
		this.amount = amount;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
