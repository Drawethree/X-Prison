package dev.drawethree.xprison.mines.model.mine;

import com.google.gson.JsonElement;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.managers.MineManager;
import dev.drawethree.xprison.mines.model.mine.reset.ResetType;
import dev.drawethree.xprison.utils.compat.CompMaterial;
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

public class Mine implements GsonSerializable {

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
	private final BlockPalette blockPalette;

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
	private Hologram blocksLeftHologram;
	@Getter
	@Setter
	private Hologram blocksMinedHologram;
	@Getter
	@Setter
	private Hologram timedResetHologram;

	@Getter
	@Setter
	private Date nextResetDate;

	@Getter
	@Setter
	private int resetTime;

	@Getter
	private final Map<PotionEffectType, Integer> mineEffects;
	private Task mineTask;

	public Mine(MineManager manager, String name, Region region) {
		this.manager = manager;
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = null;
		this.blockPalette = new BlockPalette();
		this.blockPalette.addToPalette(CompMaterial.STONE, 100.0);
		this.resetType = ResetType.INSTANT;
		this.resetPercentage = 50.0;
		this.resetTime = 10;
		this.broadcastReset = true;
		this.totalBlocks = this.calculateTotalBlocks();
		this.currentBlocks = this.calculateCurrentBlocks();
		this.mineEffects = new HashMap<>();
		this.startTicking();
	}

	public Mine(MineManager manager, String name, Region region, Point teleportLocation, BlockPalette palette, ResetType resetType, double resetPercentage, boolean broadcastReset, Hologram blocksLeftHologram, Hologram blocksMinedHologram, Hologram timedResetHologram, Map<PotionEffectType, Integer> mineEffect, int resetTime) {
		this.manager = manager;
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = teleportLocation;
		this.blockPalette = palette;
		this.resetType = resetType;
		this.resetPercentage = resetPercentage;
		this.broadcastReset = broadcastReset;
		this.totalBlocks = this.calculateTotalBlocks();
		this.currentBlocks = this.calculateCurrentBlocks();
		this.blocksLeftHologram = blocksLeftHologram;
		this.blocksMinedHologram = blocksMinedHologram;
		this.timedResetHologram = timedResetHologram;
		this.mineEffects = mineEffect;
		this.resetTime = resetTime;
		this.nextResetDate = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(this.resetTime));
		this.updateHolograms();
		this.startTicking();
	}

	private void startTicking() {
		this.mineTask = Schedulers.sync().runRepeating(this::tick, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
	}

	public void updateHolograms() {

		if (this.blocksLeftHologram != null) {
			this.blocksLeftHologram.updateLines(this.manager.getHologramBlocksLeftLines(this));
			this.blocksLeftHologram.spawn();
		}

		if (this.blocksMinedHologram != null) {
			this.blocksMinedHologram.updateLines(this.manager.getHologramBlocksMinedLines(this));
			this.blocksMinedHologram.spawn();
		}

		if (this.timedResetHologram != null) {
			this.timedResetHologram.updateLines(this.manager.getHologramTimedResetLines(this));
			this.timedResetHologram.spawn();
		}
	}

	public void handleBlockBreak(List<Block> blocks) {

		//Remove blocks that are not in region just for safety
		blocks.removeIf(block -> !isInMine(block.getLocation()));

		this.currentBlocks -= blocks.size();

		double ratio = (double) this.currentBlocks / this.totalBlocks * 100.0;

		if (ratio <= this.resetPercentage && !this.resetting) {
			this.manager.resetMine(this);
		}

		this.updateHolograms();
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
		this.getPlayersInMine().forEach(player -> this.manager.giveMineEffects(this, player));

		if (!this.resetting && this.nextResetDate != null && System.currentTimeMillis() >= this.nextResetDate.getTime()) {
			this.manager.resetMine(this);
		}

		if (!this.resetting && this.timedResetHologram != null) {
			this.timedResetHologram.updateLines(this.manager.getHologramTimedResetLines(this));
			this.timedResetHologram.spawn();
		}
	}


	@Nonnull
	@Override
	public JsonElement serialize() {
		JsonBuilder.JsonObjectBuilder builder = JsonBuilder.object();
		builder.addIfAbsent("name", this.name);
		builder.addIfAbsent("teleport-location", this.teleportLocation);
		builder.addIfAbsent("region", this.mineRegion);
		builder.addIfAbsent("blocks", this.blockPalette);
		builder.addIfAbsent("reset-type", this.resetType.getName());
		builder.addIfAbsent("reset-percentage", this.resetPercentage);
		builder.addIfAbsent("reset-time", this.resetTime);
		builder.addIfAbsent("broadcast-reset", this.broadcastReset);
		builder.addIfAbsent("hologram-blocks-mined", this.blocksMinedHologram);
		builder.addIfAbsent("hologram-blocks-left", this.blocksLeftHologram);
		builder.addIfAbsent("hologram-timed-reset", this.timedResetHologram);

		JsonBuilder.JsonObjectBuilder effectsBuilder = JsonBuilder.object();

		for (PotionEffectType type : this.mineEffects.keySet()) {
			effectsBuilder.addIfAbsent(type.getName(), this.mineEffects.get(type));
		}

		builder.addIfAbsent("effects", effectsBuilder.build());

		return builder.build();
	}

	public File getFile() {
		return new File(this.manager.getPlugin().getCore().getDataFolder().getPath() + "/mines/" + this.getName() + ".json");
	}

	public List<Player> getPlayersInMine() {
		return Players.all().stream().filter(player -> this.isInMine(player.getLocation())).collect(Collectors.toList());
	}

	public void updateCurrentBlocks() {
		this.currentBlocks = this.calculateCurrentBlocks();
	}

	public void increaseEffect(PotionEffectType type) {
		this.mineEffects.put(type, this.mineEffects.getOrDefault(type, 0) + 1);
	}

	public PotionEffect getEffect(PotionEffectType type) {
		if (this.mineEffects.containsKey(type)) {
			return new PotionEffect(type, 200, this.mineEffects.get(type));
		}
		return null;
	}

	public void decreaseEffect(PotionEffectType type) {
		int newAmplifier = this.mineEffects.getOrDefault(type, 0) - 1;
		if (newAmplifier < 0) {
			newAmplifier = 0;
		}
		this.mineEffects.put(type, newAmplifier);
	}

	public void disableEffect(PotionEffectType type) {
		this.mineEffects.remove(type);
	}

	public boolean isEffectEnabled(PotionEffectType type) {
		return this.mineEffects.containsKey(type);
	}

	public void enableEffect(PotionEffectType type) {
		this.mineEffects.put(type, 0);
	}

	public int getEffectLevel(PotionEffectType type) {
		return this.mineEffects.getOrDefault(type, 0);
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
		private BlockPalette blockPalette;
		private double resetPercentage;
		private ResetType resetType;
		private boolean broadcastReset;
		private Hologram blocksMinedHologram;
		private Hologram blocksLeftHologram;
		private Hologram timedResetHologram;
		private Map<PotionEffectType, Integer> mineEffects;
		private int timedReset;

		public Builder() {
			this.name = "";
			this.mineRegion = null;
			this.teleportLocation = null;
			this.blockPalette = new BlockPalette();
			this.resetPercentage = 50;
			this.resetType = ResetType.GRADUAL;
			this.broadcastReset = true;
			this.blocksMinedHologram = null;
			this.blocksLeftHologram = null;
			this.timedResetHologram = null;
			this.mineEffects = new HashMap<>();
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

		public Builder blockPalette(BlockPalette palette) {
			this.blockPalette = palette;
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

		public Builder blocksLeftHologram(Hologram blocksLeftHologram) {
			this.blocksLeftHologram = blocksLeftHologram;
			return this;
		}

		public Builder blocksMinedHologram(Hologram blocksMinedHologram) {
			this.blocksMinedHologram = blocksMinedHologram;
			return this;
		}

		public Builder timedResetHologram(Hologram timedResetHologram) {
			this.timedResetHologram = blocksMinedHologram;
			return this;
		}

		public Builder mineEffects(Map<PotionEffectType, Integer> mineEffects) {
			this.mineEffects = mineEffects;
			return this;
		}

		public Builder timedReset(int minutes) {
			this.timedReset = minutes;
			return this;
		}

		public Mine build() {
			return new Mine(XPrisonMines.getInstance().getManager(), this.name, this.mineRegion, this.teleportLocation, this.blockPalette, this.resetType, this.resetPercentage, this.broadcastReset, this.blocksLeftHologram, this.blocksMinedHologram, this.timedResetHologram, this.mineEffects, this.timedReset);
		}

	}
}
