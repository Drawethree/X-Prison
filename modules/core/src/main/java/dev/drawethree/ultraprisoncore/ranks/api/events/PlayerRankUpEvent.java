package dev.drawethree.ultraprisoncore.ranks.api.events;

import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import dev.drawethree.ultraprisoncore.ranks.model.Rank;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerRankUpEvent extends UltraPrisonPlayerEvent implements Cancellable {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private Rank oldRank;

	@Getter
	@Setter
	private Rank newRank;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Called when player receive gems
	 *
	 * @param player Player
	 * @param oldR   old rank
	 * @param newR   new rank
	 */
	public PlayerRankUpEvent(Player player, Rank oldR, Rank newR) {
		super(player);
		this.oldRank = oldR;
		this.newRank = newR;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
