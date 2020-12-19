package me.drawethree.ultraprisoncore.api.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.codemc.worldguardwrapper.region.IWrappedRegion;


@Getter
public class UltraPrisonAutoSellEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final IWrappedRegion region;
	private final Block block;
	@Setter
	private double moneyToDeposit;
	@Setter
	private boolean cancelled;

	public UltraPrisonAutoSellEvent(Player player, IWrappedRegion reg, Block block, double moneyToDeposit) {
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
