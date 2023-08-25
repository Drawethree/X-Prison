package dev.drawethree.xprison.utils.block;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class CuboidExplosionBlockProvider implements ExplosionBlockProvider {

	private static CuboidExplosionBlockProvider INSTANCE;

	private CuboidExplosionBlockProvider() {
	}

	@Override
	public List<Block> provide(Block center, int radius) {
		List<Block> blocks = new ArrayList<>();
		final Location startLocation = center.getLocation();
		final World world = center.getWorld();
		for (int x = startLocation.getBlockX() - (radius == 4 ? 0 : (radius / 2)); x <= startLocation.getBlockX() + (radius == 4 ? radius - 1 : (radius / 2)); x++) {
			for (int z = startLocation.getBlockZ() - (radius == 4 ? 0 : (radius / 2)); z <= startLocation.getBlockZ() + (radius == 4 ? radius - 1 : (radius / 2)); z++) {
				for (int y = startLocation.getBlockY() - (radius == 4 ? 3 : (radius / 2)); y <= startLocation.getBlockY() + (radius == 4 ? 0 : (radius / 2)); y++) {
					Block block = world.getBlockAt(x, y, z);
					blocks.add(block);
				}
			}
		}
		return blocks;
	}

	public static CuboidExplosionBlockProvider instance() {
		if (INSTANCE == null) {
			INSTANCE = new CuboidExplosionBlockProvider();
		}
		return INSTANCE;
	}
}
