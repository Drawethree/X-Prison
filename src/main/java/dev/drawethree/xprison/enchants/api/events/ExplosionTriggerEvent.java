package dev.drawethree.xprison.enchants.api.events;


import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;

@Getter
public final class ExplosionTriggerEvent extends XPrisonPlayerEnchantTriggerEvent {

	private static final HandlerList HANDLERS_LIST = new HandlerList();
	private boolean cancelled;

	/**
	 * Called when explosive enchant procs
	 *
	 * @param p              Player
	 * @param mineRegion     WorldGuard region where it was triggered
	 * @param originBlock    Original block broken that triggered it
	 * @param blocksAffected List of affected blocks (marked for removal)
	 */
	public ExplosionTriggerEvent(Player p, IWrappedRegion mineRegion, Block originBlock, List<Block> blocksAffected) {
		super(p, mineRegion, originBlock, blocksAffected);
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
