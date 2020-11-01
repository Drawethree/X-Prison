package me.drawethree.wildprisoncore.api.events.player;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.wildprisoncore.api.enums.ReceiveCause;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class WildPrisonPlayerTokensReceiveEvent extends WildPrisonPlayerEvent implements Cancellable {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final ReceiveCause cause;
	@Getter
	@Setter
	private long amount;

	@Getter
	@Setter
	private boolean cancelled;

	public WildPrisonPlayerTokensReceiveEvent(ReceiveCause cause, OfflinePlayer player, long amount) {
		super(player);
		this.cause = cause;
		this.amount = amount;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
