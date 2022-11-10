package dev.drawethree.ultraprisoncore.gems.api.events;

import dev.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class PlayerGemsReceiveEvent extends UltraPrisonPlayerEvent implements Cancellable {


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
	 * Called when player receive gems
	 *
	 * @param cause  ReceiveCause
	 * @param player Player
	 * @param amount Amount of gems received
	 */
	public PlayerGemsReceiveEvent(ReceiveCause cause, OfflinePlayer player, long amount) {
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
