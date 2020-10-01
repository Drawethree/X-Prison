package me.drawethree.wildprisoncore.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class WildPrisonBlockBreakEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();


	private Player player;
	private long amount;
	private boolean cancelled;

	public WildPrisonBlockBreakEvent(Player player, long amount) {
		this.player = player;
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
