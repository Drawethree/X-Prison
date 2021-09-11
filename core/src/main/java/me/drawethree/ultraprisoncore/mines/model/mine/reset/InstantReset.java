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

		for (int x = (int) min.getX(); x < max.getX(); x++) {
			for (int y = (int) min.getY(); y < max.getY(); y++) {
				for (int z = (int) min.getZ(); z < max.getZ(); z++) {
					Block b = min.toLocation().getWorld().getBlockAt(x, y, z);
					CompMaterial pick = selector.pick();
					//b.setType(pick.toMaterial());
					UltraPrisonCore.getInstance().getNmsProvider().setBlockInNativeDataPalette(b.getWorld(), x, y, z, pick.getMaterial().getId(), (byte) pick.getData(), true);
				}
			}
		}
	}
}
