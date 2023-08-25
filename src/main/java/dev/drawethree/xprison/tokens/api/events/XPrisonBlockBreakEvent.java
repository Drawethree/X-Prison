package dev.drawethree.xprison.tokens.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.List;

@Getter
@Setter
public final class XPrisonBlockBreakEvent extends XPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private Player player;
	private List<Block> blocks;
	private boolean cancelled;

	/**
	 * Called when player mines blocks within mine with or without custom enchants.
	 *
	 * @param player Player
	 * @param blocks List of blocks that were affected
	 */
	public XPrisonBlockBreakEvent(Player player, List<Block> blocks) {
		super(player);
		this.player = player;
		this.blocks = blocks;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
