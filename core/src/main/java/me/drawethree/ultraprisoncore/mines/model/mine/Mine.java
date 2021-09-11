package me.drawethree.ultraprisoncore.mines.model.mine;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.model.mine.reset.ResetType;
import me.lucko.helper.Schedulers;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import me.lucko.helper.utils.Players;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Mine implements GsonSerializable {

	@Getter
	private String name;
	@Getter
	private Region mineRegion;

	@Getter
	@Setter
	private Position teleportLocation;

	@Getter
	private BlockPalette blockPalette;

	@Getter
	@Setter
	private double resetPercentage;

	@Getter
	private int totalBlocks;
	@Getter
	@Setter
	private ResetType resetType;

	@Getter
	private boolean resetting;

	public Mine(String name, Region region) {
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = null;
		this.blockPalette = new BlockPalette();
		this.resetType = ResetType.INSTANT;
		this.resetPercentage = 50.0;
		this.totalBlocks = this.calculateTotalBlocks();
	}

	public Mine(String name, Region region, Position teleportLocation, BlockPalette palette, ResetType resetType, double resetPercentage) {
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = teleportLocation;
		this.blockPalette = palette;
		this.resetType = resetType;
		this.resetPercentage = resetPercentage;
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

		this.getPlayersInMine().forEach(player -> {
			player.sendMessage(UltraPrisonMines.getInstance().getMessage("mine_resetting").replace("%mine%", this.name));
		});

		Schedulers.sync().runLater(() -> {
			this.resetting = true;

			this.getPlayersInMine().forEach(player -> player.teleport(this.teleportLocation.toLocation()));
			this.resetType.reset(this, this.blockPalette);
			this.getPlayersInMine().forEach(player -> {
				player.sendMessage(UltraPrisonMines.getInstance().getMessage("mine_reset").replace("%mine%", this.name));
			});

			this.resetting = false;
		}, 5, TimeUnit.SECONDS);
	}

	@Nonnull
	@Override
	public JsonElement serialize() {
		return JsonBuilder.object()
				.addIfAbsent("name", this.name)
				.addIfAbsent("teleport-location", this.teleportLocation)
				.addIfAbsent("region", this.mineRegion)
				.addIfAbsent("blocks", this.blockPalette)
				.addIfAbsent("reset-type", this.resetType.getName())
				.addIfAbsent("reset-percentage", this.resetPercentage)
				.build();
	}

	public File getFile() {
		return new File(UltraPrisonCore.getInstance().getDataFolder().getPath() + "/mines/" + this.getName());
	}

	public List<Player> getPlayersInMine() {
		return Players.all().stream().filter(player -> this.isInMine(player.getLocation())).collect(Collectors.toList());
	}
}
