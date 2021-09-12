package me.drawethree.ultraprisoncore.mines.model.mine.reset;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.block.Block;

import java.util.Iterator;

public class GradualReset extends ResetType {

	private final int CHANGES_PER_TICK = 350;

	GradualReset() {
		super("Gradual");
	}

	@Override
	public void reset(Mine paramMine, BlockPalette blockPalette) {
		this.schedule(paramMine, blockPalette, paramMine.getBlocksIterator());
	}


	private void schedule(final Mine mine, BlockPalette blockPalette, Iterator<Block> blocksIterator) {
		Schedulers.sync().runLater(() -> {
			int changes = 0;
			RandomSelector<CompMaterial> selector = RandomSelector.weighted(blockPalette.getMaterials(), blockPalette::getPercentage);
			while (blocksIterator.hasNext() && changes <= CHANGES_PER_TICK) {
				CompMaterial pick = selector.pick();
				Block b = blocksIterator.next();
				UltraPrisonCore.getInstance().getNmsProvider().setBlockInNativeDataPalette(b.getWorld(), b.getX(), b.getY(), b.getZ(), pick.getMaterial().getId(), pick.getData(), true);
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