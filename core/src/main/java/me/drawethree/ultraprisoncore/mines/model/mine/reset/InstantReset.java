package me.drawethree.ultraprisoncore.mines.model.mine.reset;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.serialize.Position;
import org.bukkit.block.Block;


public class InstantReset extends ResetType {

	InstantReset() {
		super("Instant");
	}

	@Override
	public void reset(Mine mine, BlockPalette blockPalette) {
		RandomSelector<CompMaterial> selector = RandomSelector.weighted(blockPalette.getMaterials(), blockPalette::getPercentage);

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
					//b.setType(pick.toMaterial());
					UltraPrisonCore.getInstance().getNmsProvider().setBlockInNativeDataPalette(b.getWorld(), x, y, z, pick.getMaterial().getId(), (byte) pick.getData(), true);
				}
			}
		}
		mine.setResetting(false);
		mine.updateCurrentBlocks();
		mine.updateHolograms();
	}
}
