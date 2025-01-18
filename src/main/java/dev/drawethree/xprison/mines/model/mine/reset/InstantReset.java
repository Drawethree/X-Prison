package dev.drawethree.xprison.mines.model.mine.reset;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.mines.model.mine.BlockPalette;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.serialize.Position;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class InstantReset extends ResetType {

	InstantReset() {
		super("Instant");
	}

	@Override
	public void reset(Mine mine, BlockPalette blockPalette) {

		if (blockPalette.isEmpty()) {
			XPrison.getInstance().getLogger().warning("Reset for Mine " + mine.getName() + " aborted. Block palette is empty.");
			return;
		}

		RandomSelector<CompMaterial> selector = RandomSelector.weighted(blockPalette.getValidMaterials(), blockPalette::getPercentage);

		Position min = mine.getMineRegion().getMin();
		Position max = mine.getMineRegion().getMax();

		int minX = (int) Math.min(min.getX(), max.getX());
		int minY = (int) Math.min(min.getY(), max.getY());
		int minZ = (int) Math.min(min.getZ(), max.getZ());

		int maxX = (int) Math.max(min.getX(), max.getX());
		int maxY = (int) Math.max(min.getY(), max.getY());
		int maxZ = (int) Math.max(min.getZ(), max.getZ());

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block b = min.toLocation().getWorld().getBlockAt(x, y, z);
					CompMaterial pick = selector.pick();
					b.setType(pick.toMaterial());
					if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
						try {
							//ANTES: Block.class.getMethod("setData", byte.class).invoke(b, pick.getData());
                            Material material = b.getType();
							BlockData blockData = material.createBlockData();

							if (blockData instanceof org.bukkit.block.data.Directional) {
								org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) blockData;
								directional.setFacing(org.bukkit.block.BlockFace.NORTH);
							}

							b.setBlockData(blockData);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		mine.setResetting(false);
		mine.updateCurrentBlocks();
		mine.updateHolograms();
	}
}
