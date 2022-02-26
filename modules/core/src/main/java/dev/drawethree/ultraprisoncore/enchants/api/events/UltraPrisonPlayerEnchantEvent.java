package dev.drawethree.ultraprisoncore.enchants.api.events;

import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class UltraPrisonPlayerEnchantEvent extends UltraPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	@Getter
	@Setter
	private final long tokenCost;
	@Getter
	private final int level;
	@Getter
	@Setter
	private boolean cancelled;


	/**
	 * Called when player enchants a tool
	 *
	 * @param player    Player
	 * @param tokenCost cost of enchant in tokens
	 * @param level     level of enchant
	 */
	public UltraPrisonPlayerEnchantEvent(Player player, long tokenCost, int level) {
		super(player);
		this.tokenCost = tokenCost;
		this.level = level;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
