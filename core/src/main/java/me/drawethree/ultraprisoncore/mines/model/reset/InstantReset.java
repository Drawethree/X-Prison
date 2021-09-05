package me.drawethree.ultraprisoncore.mines.model.reset;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.serialize.Position;
import org.bukkit.block.Block;

import java.util.Map;

public class InstantReset extends ResetType {

	InstantReset() {
		super("Instant");
	}

	@Override
	public void reset(Mine mine, Map<CompMaterial, Double> blocks) {
		RandomSelector<CompMaterial> selector = RandomSelector.weighted(blocks.keySet(), blocks::get);

		Position min = mine.getMineRegion().getMin();
		Position max = mine.getMineRegion().getMax();

		for (int x = (int) min.getX(); x < max.getX(); x++) {
			for (int y = (int) min.getY(); y < max.getY(); y++) {
				for (int z = (int) min.getZ(); z < max.getZ(); z++) {
					Block b = min.toLocation().getWorld().getBlockAt(x, y, z);
					CompMaterial pick = selector.pick();
					b.setType(pick.toMaterial());
				}
			}
		}
	}
}
