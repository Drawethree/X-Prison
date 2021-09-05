package me.drawethree.ultraprisoncore.mines.model.mine;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.mines.model.reset.ResetType;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Mine implements GsonSerializable {

	@Getter
	private String name;
	@Getter
	private Region mineRegion;
	@Getter
	@Setter
	private Position teleportLocation;
	private Map<CompMaterial, Double> blockPercentages;
	private double resetPercentage;
	private int totalBlocks;
	private ResetType resetType;

	public Mine(String name, Region region) {
		this.mineRegion = region;
		this.teleportLocation = null;
		this.blockPercentages = new HashMap<>();
		this.resetPercentage = 50.0;
		this.totalBlocks = this.calculateTotalBlocks();
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
		this.resetType.reset(this, this.blockPercentages);
	}

	@Nonnull
	@Override
	public JsonElement serialize() {
		return null;
	}
}
