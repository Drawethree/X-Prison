package dev.drawethree.xprison.mines.model.mine.reset;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.BlockPaletteImpl;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.warning;

public class GradualReset extends ResetType {

	private final int CHANGES_PER_TICK = 350;

	GradualReset() {
		super("Gradual");
	}

	@Override
	public void reset(MineImpl paramMineImpl, BlockPaletteImpl blockPaletteImpl) {

		if (blockPaletteImpl.isEmpty()) {
			warning("Reset for Mine " + paramMineImpl.getName() + " aborted. Block palette is empty.");
			return;
		}

		this.schedule(paramMineImpl, blockPaletteImpl, paramMineImpl.getBlocksIterator());
	}


	private void schedule(final MineImpl mineImpl, BlockPaletteImpl blockPaletteImpl, Iterator<Block> blocksIterator) {
		Schedulers.sync().runLater(() -> {
			int changes = 0;
			RandomSelector<XMaterial> selector = RandomSelector.weighted(blockPaletteImpl.getValidMaterials(), blockPaletteImpl::getPercentage);
			while (blocksIterator.hasNext() && changes <= CHANGES_PER_TICK) {
				XMaterial pick = selector.pick();
				Block b = blocksIterator.next();
				b.setType(pick.get());
				if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
					try {
						Block.class.getMethod("setData", byte.class).invoke(b, pick.getData());
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						error("Exception happened during Gradual Reset");
						e.printStackTrace();
					}
				}
				changes++;
			}
			if (blocksIterator.hasNext()) {
				schedule(mineImpl, blockPaletteImpl, blocksIterator);
			} else {
				mineImpl.setResetting(false);
				mineImpl.updateCurrentBlocks();
				mineImpl.updateHolograms();
			}
		}, 1L);
	}
}