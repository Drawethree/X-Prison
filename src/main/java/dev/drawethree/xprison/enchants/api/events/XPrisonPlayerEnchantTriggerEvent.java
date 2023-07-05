package dev.drawethree.xprison.enchants.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;

@Getter
public abstract class XPrisonPlayerEnchantTriggerEvent extends XPrisonPlayerEvent implements Cancellable {

	protected final Player player;
	protected final IWrappedRegion mineRegion;
	protected final Block originBlock;
	protected final List<Block> blocksAffected;

	public XPrisonPlayerEnchantTriggerEvent(Player p, IWrappedRegion mineRegion, Block originBlock, List<Block> blocksAffected) {
		super(p);
		this.player = p;
		this.mineRegion = mineRegion;
		this.originBlock = originBlock;
		this.blocksAffected = blocksAffected;
	}
}
