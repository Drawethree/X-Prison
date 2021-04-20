package me.drawethree.ultraprisoncore.api.events.player;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class UltraPrisonPlayerRankUpEvent extends UltraPrisonPlayerEvent implements Cancellable {


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
	public UltraPrisonPlayerRankUpEvent(Player player, Rank oldR, Rank newR) {
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
