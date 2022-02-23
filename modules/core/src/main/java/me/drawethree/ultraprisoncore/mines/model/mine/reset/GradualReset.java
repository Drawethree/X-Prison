package me.drawethree.ultraprisoncore.mines.model.mine.reset;

import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.drawethree.ultraprisoncore.utils.compat.MinecraftVersion;
import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.block.Block;

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
			blockPalette.addToPalette(CompMaterial.STONE, 100.0);
		}
		this.schedule(paramMine, blockPalette, paramMine.getBlocksIterator());
	}


	private void schedule(final Mine mine, BlockPalette blockPalette, Iterator<Block> blocksIterator) {
		Schedulers.sync().runLater(() -> {
			int changes = 0;
			RandomSelector<CompMaterial> selector = RandomSelector.weighted(blockPalette.getMaterials(), blockPalette::getPercentage);
			while (blocksIterator.hasNext() && changes <= CHANGES_PER_TICK) {
				CompMaterial pick = selector.pick();
				Block b = blocksIterator.next();
				b.setType(pick.toMaterial());
				if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
					try {
						Block.class.getMethod("setData", byte.class).invoke(b, pick.getData());
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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