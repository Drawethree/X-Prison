package dev.drawethree.ultraprisoncore.enchants.api.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;

@Getter
public abstract class BlockEnchantEvent extends Event implements Cancellable {

	protected final Player player;
	protected final IWrappedRegion mineRegion;
	protected final List<Block> blocksAffected;

	public BlockEnchantEvent(Player p, IWrappedRegion mineRegion, List<Block> blocksAffected) {
		this.player = p;
		this.mineRegion = mineRegion;
		this.blocksAffected = blocksAffected;
	}
}
