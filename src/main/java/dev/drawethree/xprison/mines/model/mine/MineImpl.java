package dev.drawethree.xprison.mines.model.mine;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.JsonElement;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.managers.MineManager;
import dev.drawethree.xprison.mines.model.mine.reset.ResetType;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.hologram.Hologram;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import me.lucko.helper.utils.Players;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MineImpl implements GsonSerializable {

	@Getter
	private final MineManager manager;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private Region mineRegion;

	@Getter
	@Setter
	private Point teleportLocation;

	@Getter
	private final BlockPaletteImpl blockPaletteImpl;

	@Getter
	@Setter
	private double resetPercentage;

	@Getter
	private final int totalBlocks;

	@Getter
	@Setter
	private int currentBlocks;

	@Getter
	@Setter
	private ResetType resetType;

	@Getter
	@Setter
	private boolean resetting;

	@Getter
	@Setter
	private boolean broadcastReset;

	@Getter
	@Setter
	private Date nextResetDate;

	@Getter
	@Setter
	private int resetTime;

	private Task mineTask;

	public MineImpl(MineManager manager, String name, Region region) {
		this.manager = manager;
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = null;
		this.blockPaletteImpl = new BlockPaletteImpl();
		this.blockPaletteImpl.add(XMaterial.STONE, 100.0);
		this.resetType = ResetType.INSTANT;
		this.resetPercentage = 50.0;
		this.resetTime = 10;
		this.broadcastReset = true;
		this.totalBlocks = this.calculateTotalBlocks();
		this.currentBlocks = this.calculateCurrentBlocks();
		this.startTicking();
	}

	public MineImpl(MineManager manager, String name, Region region, Point teleportLocation, BlockPaletteImpl palette, ResetType resetType, double resetPercentage, boolean broadcastReset, int resetTime) {
		this.manager = manager;
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = teleportLocation;
		this.blockPaletteImpl = palette;
		this.resetType = resetType;
		this.resetPercentage = resetPercentage;
		this.broadcastReset = broadcastReset;
		this.totalBlocks = this.calculateTotalBlocks();
		this.currentBlocks = this.calculateCurrentBlocks();
		this.resetTime = resetTime;
		this.nextResetDate = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(this.resetTime));
		this.startTicking();
	}

	private void startTicking() {
		this.mineTask = Schedulers.sync().runRepeating(this::tick, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
	}


	public void handleBlockBreak(List<Block> blocks) {

		//Remove blocks that are not in region just for safety
		blocks.removeIf(block -> !isInMine(block.getLocation()));

		this.currentBlocks -= blocks.size();

		double ratio = (double) this.currentBlocks / this.totalBlocks * 100.0;

		if (ratio <= this.resetPercentage && !this.resetting) {
			this.manager.resetMine(this);
		}

	}

	public void stopTicking() {
		if (this.mineTask != null) {
			this.mineTask.stop();
		}
	}

	public boolean isInMine(Location loc) {
		return this.mineRegion.inRegion(loc);
	}

	public Iterator<Block> getBlocksIterator() {
		Position min = this.mineRegion.getMin();
		Position max = this.mineRegion.getMax();

		int minX = (int) Math.min(min.getX(), max.getX());
		int minY = (int) Math.min(min.getY(), max.getY());
		int minZ = (int) Math.min(min.getZ(), max.getZ());

		int maxX = (int) Math.max(min.getX(), max.getX());
		int maxY = (int) Math.max(min.getY(), max.getY());
		int maxZ = (int) Math.max(min.getZ(), max.getZ());

		List<Block> blocks = new ArrayList<>(1000);
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block b = min.toLocation().getWorld().getBlockAt(x, y, z);
					blocks.add(b);
				}
			}
		}
		return blocks.iterator();
	}

	private int calculateCurrentBlocks() {
		Position min = this.mineRegion.getMin();
		Position max = this.mineRegion.getMax();

		int minX = (int) Math.min(min.getX(), max.getX());
		int minY = (int) Math.min(min.getY(), max.getY());
		int minZ = (int) Math.min(min.getZ(), max.getZ());

		int maxX = (int) Math.max(min.getX(), max.getX());
		int maxY = (int) Math.max(min.getY(), max.getY());
		int maxZ = (int) Math.max(min.getZ(), max.getZ());

		int amount = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block b = min.toLocation().getWorld().getBlockAt(x, y, z);
					if (b.getType() != Material.AIR) {
						amount++;
					}
				}
			}
		}
		return amount;
	}


	private int calculateTotalBlocks() {
		Position min = this.mineRegion.getMin();
		Position max = this.mineRegion.getMax();

		int minX = (int) Math.min(min.getX(), max.getX());
		int minY = (int) Math.min(min.getY(), max.getY());
		int minZ = (int) Math.min(min.getZ(), max.getZ());

		int maxX = (int) Math.max(min.getX(), max.getX());
		int maxY = (int) Math.max(min.getY(), max.getY());
		int maxZ = (int) Math.max(min.getZ(), max.getZ());

		int amount = 0;
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					amount++;
				}
			}
		}
		return amount;
	}

	private void tick() {
		if (!this.resetting && this.nextResetDate != null && System.currentTimeMillis() >= this.nextResetDate.getTime()) {
			this.manager.resetMine(this);
		}
	}


	@Nonnull
	@Override
	public JsonElement serialize() {
		JsonBuilder.JsonObjectBuilder builder = JsonBuilder.object();
		builder.addIfAbsent("name", this.name);
		builder.addIfAbsent("teleport-location", this.teleportLocation);
		builder.addIfAbsent("region", this.mineRegion);
		builder.addIfAbsent("blocks", this.blockPaletteImpl);
		builder.addIfAbsent("reset-type", this.resetType.getName());
		builder.addIfAbsent("reset-percentage", this.resetPercentage);
		builder.addIfAbsent("reset-time", this.resetTime);
		builder.addIfAbsent("broadcast-reset", this.broadcastReset);
		return builder.build();
	}

	public File getFile() {
		return new File(this.manager.getPlugin().getCore().getDataFolder().getPath() + "/mines/" + this.getName() + ".json");
	}

	public Region getRegion() {
		return mineRegion;
	}

	public BlockPaletteImpl getBlockPalette() {
		return blockPaletteImpl;
	}

	public List<Player> getPlayersInMine() {
		return Players.all().stream().filter(player -> this.isInMine(player.getLocation())).collect(Collectors.toList());
	}

	public void updateCurrentBlocks() {
		this.currentBlocks = this.calculateCurrentBlocks();
	}

	public boolean canTeleport(Player player) {
		return player.hasPermission("xprison.mines.tp." + this.name);
	}

	public long getSecondsToNextReset() {
		if (this.nextResetDate == null) {
			return 0;
		}
		return (int) ((this.nextResetDate.getTime() - System.currentTimeMillis()) / 1000);
	}

	public static final class Builder {

		private String name;
		private Region mineRegion;
		private Point teleportLocation;
		private BlockPaletteImpl blockPaletteImpl;
		private double resetPercentage;
		private ResetType resetType;
		private boolean broadcastReset;
		private int timedReset;

		public Builder() {
			this.name = "";
			this.mineRegion = null;
			this.teleportLocation = null;
			this.blockPaletteImpl = new BlockPaletteImpl();
			this.resetPercentage = 50;
			this.resetType = ResetType.INSTANT;
			this.broadcastReset = true;
			this.timedReset = 10;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder region(Region region) {
			this.mineRegion = region;
			return this;
		}

		public Builder teleportLocation(Point point) {
			this.teleportLocation = point;
			return this;
		}

		public Builder blockPalette(BlockPaletteImpl palette) {
			this.blockPaletteImpl = palette;
			return this;
		}

		public Builder resetPercentage(double resetPercentage) {
			this.resetPercentage = resetPercentage;
			return this;
		}

		public Builder resetType(ResetType resetType) {
			this.resetType = resetType;
			return this;
		}

		public Builder broadcastReset(boolean broadcastReset) {
			this.broadcastReset = broadcastReset;
			return this;
		}

		public Builder timedReset(int minutes) {
			this.timedReset = minutes;
			return this;
		}

		public MineImpl build() {
			return new MineImpl(XPrisonMines.getInstance().getManager(), this.name, this.mineRegion, this.teleportLocation, this.blockPaletteImpl, this.resetType, this.resetPercentage, this.broadcastReset, this.timedReset);
		}

	}
}
