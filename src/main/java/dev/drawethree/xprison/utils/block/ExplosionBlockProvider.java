package dev.drawethree.xprison.utils.block;

import org.bukkit.block.Block;

import java.util.List;

public interface ExplosionBlockProvider {

	List<Block> provide(Block center, int radius);
}
