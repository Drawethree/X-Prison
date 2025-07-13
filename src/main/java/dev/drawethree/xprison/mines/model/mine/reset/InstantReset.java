package dev.drawethree.xprison.mines.model.mine.reset;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.mines.model.mine.BlockPaletteImpl;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.serialize.Position;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.warning;


public class InstantReset extends ResetType {

	InstantReset() {
		super("Instant");
	}

	@Override
	public void reset(MineImpl mineImpl, BlockPaletteImpl blockPaletteImpl) {

		if (blockPaletteImpl.isEmpty()) {
			warning("Reset for Mine " + mineImpl.getName() + " aborted. Block palette is empty.");
			return;
		}

		RandomSelector<XMaterial> selector = RandomSelector.weighted(blockPaletteImpl.getValidMaterials(), blockPaletteImpl::getPercentage);

		Position min = mineImpl.getMineRegion().getMin();
		Position max = mineImpl.getMineRegion().getMax();

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
					XMaterial pick = selector.pick();
					b.setType(pick.get());
					if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
						try {
							Block.class.getMethod("setData", byte.class).invoke(b, pick.getData());
						} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
							error("Exception happened during Instant Reset");
							e.printStackTrace();
						}
					}
				}
			}
		}
		mineImpl.setResetting(false);
		mineImpl.updateCurrentBlocks();
	}
}
