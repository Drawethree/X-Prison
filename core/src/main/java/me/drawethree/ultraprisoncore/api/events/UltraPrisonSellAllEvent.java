package me.drawethree.ultraprisoncore.api.events;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.autosell.SellRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@Getter
public class UltraPrisonSellAllEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final SellRegion region;

	@Getter
	@Setter
	private double sellPrice;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Called when mined blocks are automatically sold
	 *
	 * @param player    Player
	 * @param reg       SellRegion where block was mined
	 * @param sellPrice Amount what will player receive after selling
	 */
	public UltraPrisonSellAllEvent(Player player, SellRegion reg, double sellPrice) {
		this.player = player;
		this.region = reg;
		this.sellPrice = sellPrice;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
