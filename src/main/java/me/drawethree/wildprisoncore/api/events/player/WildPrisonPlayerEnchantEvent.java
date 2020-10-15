package me.drawethree.wildprisoncore.api.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class WildPrisonPlayerEnchantEvent extends WildPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	@Setter
	private final long tokenCost;

	@Getter
	@Setter
	private boolean cancelled;

	@Getter
	private final int level;


	public WildPrisonPlayerEnchantEvent(Player player, long tokenCost, int level) {
		super(player);
		this.tokenCost = tokenCost;
		this.level = level;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
