package me.drawethree.ultraprisoncore.mines.model;

import com.google.gson.JsonElement;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Mine implements GsonSerializable {

	private String name;
	private Region mineRegion;
	private Position spawnLocation;
	private Map<CompMaterial, Double> blockPercentages;
	private double resetPercentage;
	private int totalBlocks;

	public Mine(String name, Region region) {
		this.mineRegion = region;
		this.spawnLocation = null;
		this.blockPercentages = new HashMap<>();
		this.resetPercentage = 50.0;
		this.totalBlocks = calculateTotalBlocks();
	}

	public void handleBlockBreaks() {

		double ratio = (double) this.getBlocksLeft() / this.totalBlocks;

		if (ratio <= this.resetPercentage) {
			this.resetMine();
		}
	}

	public boolean isInMine(Location loc) {
		return this.mineRegion.inRegion(loc);
	}

	private int getBlocksLeft() {
		Position min = this.mineRegion.getMin();
		Position max = this.mineRegion.getMax();
		int count = 0;
		for (int x = (int) min.getX(); x < max.getX(); x++) {
			for (int y = (int) min.getY(); y < max.getY(); y++) {
				for (int z = (int) min.getZ(); z < max.getZ(); z++) {
					Block b = min.toLocation().getWorld().getBlockAt(x, y, z);
					if (b != null && b.getType() != Material.AIR) {
						count++;
					}
				}
			}
		}
		return count;
	}


	private int calculateTotalBlocks() {
		Position min = this.mineRegion.getMin();
		Position max = this.mineRegion.getMax();
		int amount = 0;
		for (int x = (int) min.getX(); x < max.getX(); x++) {
			for (int y = (int) min.getY(); y < max.getY(); y++) {
				for (int z = (int) min.getZ(); z < max.getZ(); z++) {
					amount++;
				}
			}
		}
		return amount;
	}

	public void resetMine() {
		RandomSelector<CompMaterial> selector = RandomSelector.weighted(this.blockPercentages.keySet(), (material) -> this.blockPercentages.get(material));

		Position min = this.mineRegion.getMin();
		Position max = this.mineRegion.getMax();

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

	@Nonnull
	@Override
	public JsonElement serialize() {
		return null;
	}
}
