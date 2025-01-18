package dev.drawethree.xprison.mines.model.mine.reset;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.mines.model.mine.BlockPalette;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

public class GradualReset extends ResetType {

	private final int CHANGES_PER_TICK = 350;

	GradualReset() {
		super("Gradual");
	}

	@Override
	public void reset(Mine paramMine, BlockPalette blockPalette) {

		if (blockPalette.isEmpty()) {
			XPrison.getInstance().getLogger().warning("Reset for Mine " + paramMine.getName() + " aborted. Block palette is empty.");
			return;
		}

		this.schedule(paramMine, blockPalette, paramMine.getBlocksIterator());
	}


	private void schedule(final Mine mine, BlockPalette blockPalette, Iterator<Block> blocksIterator) {
		Schedulers.sync().runLater(() -> {
			int changes = 0;
			RandomSelector<CompMaterial> selector = RandomSelector.weighted(blockPalette.getValidMaterials(), blockPalette::getPercentage);
			while (blocksIterator.hasNext() && changes <= CHANGES_PER_TICK) {
				CompMaterial pick = selector.pick();
				Block b = blocksIterator.next();
				b.setType(pick.toMaterial());
				if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
					try {
						//Block.class.getMethod("setData", byte.class).invoke(b, pick.getData());
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
				changes++;
			}
			if (blocksIterator.hasNext()) {
				schedule(mine, blockPalette, blocksIterator);
			} else {
				mine.setResetting(false);
				mine.updateCurrentBlocks();
				mine.updateHolograms();
			}
		}, 1L);
	}
}