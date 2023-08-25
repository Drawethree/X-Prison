package dev.drawethree.xprison.utils.block;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class SpheroidExplosionBlockProvider implements ExplosionBlockProvider {

	private static SpheroidExplosionBlockProvider INSTANCE;

	private SpheroidExplosionBlockProvider() {
	}

	@Override
	public List<Block> provide(Block center, int radius) {
		List<Block> blocks = new ArrayList<>();
		for (int Y = -radius; Y < radius; Y++)
			for (int X = -radius; X < radius; X++)
				for (int Z = -radius; Z < radius; Z++)
					if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
						final Block block = center.getWorld().getBlockAt(X + center.getX(), Y + center.getY(), Z + center.getZ());
						blocks.add(block);
					}
		return blocks;
	}

	public static SpheroidExplosionBlockProvider instance() {
		if (INSTANCE == null) {
			INSTANCE = new SpheroidExplosionBlockProvider();
		}
		return INSTANCE;
	}
}
