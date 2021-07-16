package me.drawethree.ultraprisoncore.api.events;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.autosell.SellRegion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@Getter
public class UltraPrisonAutoSellEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final SellRegion region;
	private final Block block;
	@Setter
	private double moneyToDeposit;
	@Setter
	private boolean cancelled;

	/**
	 * Called when mined blocks are automatically sold
	 *
	 * @param player         Player
	 * @param reg            IWrappedRegion where block was mined
	 * @param block          Block that was mined
	 * @param moneyToDeposit Default amount to deposit
	 */
	public UltraPrisonAutoSellEvent(Player player, SellRegion reg, Block block, double moneyToDeposit) {
		this.player = player;
		this.region = reg;
		this.block = block;
		this.moneyToDeposit = moneyToDeposit;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
