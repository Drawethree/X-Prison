package me.drawethree.ultraprisoncore.ranks.api.events;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import me.drawethree.ultraprisoncore.ranks.model.Rank;
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

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
