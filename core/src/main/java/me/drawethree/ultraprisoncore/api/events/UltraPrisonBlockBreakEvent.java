package me.drawethree.ultraprisoncore.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class UltraPrisonBlockBreakEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private Player player;
	private long amount;
	private boolean cancelled;

	/**
	 * Called when player mines blocks within mine with or without custom enchants.
	 *
	 * @param player Player
	 * @param amount Amount of blocks broken simultaneously
	 */
	public UltraPrisonBlockBreakEvent(Player player, long amount) {
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
