package me.drawethree.ultraprisoncore.mines.model.mine;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.managers.MineManager;
import me.drawethree.ultraprisoncore.mines.model.mine.reset.ResetType;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.hologram.Hologram;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import me.lucko.helper.utils.Players;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Mine implements GsonSerializable {

	private MineManager manager;

	@Getter
	private String name;
	@Getter
	private Region mineRegion;

	@Getter
	@Setter
	private Point teleportLocation;

	@Getter
	private BlockPalette blockPalette;

	@Getter
	@Setter
	private double resetPercentage;

	@Getter
	private int totalBlocks;

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

	private Hologram blocksLeftHologram;
	private Hologram blocksMinedHologram;
	private Map<PotionEffectType, Integer> mineEffects;

	public Mine(MineManager manager, String name, Region region) {
		this.manager = manager;
		this.name = name;
		this.mineRegion = region;
		this.teleportLocation = null;
		this.blockPalette = new BlockPalette();
		this.resetType = ResetType.INSTANT;
		this.resetPercentage = 50.0;
		this.broadcastReset = true;
		this.totalBlocks = this.calculateTotalBlocks();
		this.currentBlocks = this.calculateCurrentBlocks();
		this.mineEffects = new HashMap<>();
		this.subscribeEvents();
		this.startTicking();
	}

	public Mine(MineManager manager, String name, Region region, Point teleportLocation, BlockPalette palette, ResetType resetType, double resetPercentage, boolean broadcastReset, Hologram blocksLeftHologram, Hologram blocksMinedHologram, Map<PotionEffectType, Integer> mineEffects) {
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
		this.mineEffects = mineEffects;
		this.updateHolograms();
		this.subscribeEvents();
		this.startTicking();
	}

	private void startTicking() {
		Schedulers.sync().runRepeating(this::tick, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
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
	}

	public void handleBlockBreak(List<Block> blocks) {

		//Remove blocks that are not in region just for safety
		blocks.removeIf(block -> !isInMine(block.getLocation()));

		this.currentBlocks -= blocks.size();

		double ratio = (double) this.currentBlocks / this.totalBlocks * 100.0;

		if (ratio <= this.resetPercentage && !this.resetting) {
			this.resetMine();
		}

		this.updateHolograms();
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
		this.getPlayersInMine().forEach(this::giveMineEffects);
	}

	private void giveMineEffects(Player player) {
		for (PotionEffectType type : this.mineEffects.keySet()) {
			player.removePotionEffect(type);
			player.addPotionEffect(this.getEffect(type));
		}
	}

	public void resetMine() {

		if (this.resetting) {
			return;
		}

		this.resetting = true;

		if (broadcastReset) {
			Players.all().forEach(player -> player.sendMessage(this.manager.getPlugin().getMessage("mine_resetting").replace("%mine%", this.name)));
		}

		Schedulers.sync().runLater(() -> {

			if (this.teleportLocation != null) {
				this.getPlayersInMine().forEach(player -> player.teleport(this.teleportLocation.toLocation()));
			}

			this.resetType.reset(this, this.blockPalette);

			if (broadcastReset) {
				Players.all().forEach(player -> player.sendMessage(this.manager.getPlugin().getMessage("mine_reset").replace("%mine%", this.name)));
			}

			this.resetting = false;
		}, 5, TimeUnit.SECONDS);
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
		builder.addIfAbsent("broadcast-reset", this.broadcastReset);
		builder.addIfAbsent("hologram-blocks-mined", this.blocksMinedHologram);
		builder.addIfAbsent("hologram-blocks-left", this.blocksLeftHologram);

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

	public void createHologram(HologramType type, Player player) {
		switch (type) {
			case BLOCKS_LEFT: {
				if (this.blocksLeftHologram == null) {
					this.blocksLeftHologram = Hologram.create(Position.of(player.getLocation()), this.manager.getHologramBlocksLeftLines(this));
					this.blocksLeftHologram.spawn();
				} else {
					this.blocksLeftHologram.despawn();
					this.blocksLeftHologram.updatePosition(Position.of(player.getLocation()));
					this.blocksLeftHologram.spawn();
				}
				break;
			}
			case BLOCKS_MINED: {
				if (this.blocksMinedHologram == null) {
					this.blocksMinedHologram = Hologram.create(Position.of(player.getLocation()), this.manager.getHologramBlocksMinedLines(this));
					this.blocksMinedHologram.spawn();
				} else {
					this.blocksMinedHologram.despawn();
					this.blocksMinedHologram.updatePosition(Position.of(player.getLocation()));
					this.blocksMinedHologram.spawn();
				}
				break;
			}
		}
		player.sendMessage(this.manager.getPlugin().getMessage("mine_hologram_create").replace("%type%", type.name()).replace("%mine%", this.name));
	}

	public void deleteHologram(HologramType type, Player player) {
		switch (type) {
			case BLOCKS_LEFT: {
				if (this.blocksLeftHologram != null) {
					this.blocksLeftHologram.despawn();
					this.blocksLeftHologram = null;
				}
				break;
			}
			case BLOCKS_MINED: {
				if (this.blocksMinedHologram != null) {
					this.blocksMinedHologram.despawn();
					this.blocksMinedHologram = null;
				}
				break;
			}
		}
		player.sendMessage(this.manager.getPlugin().getMessage("mine_hologram_delete").replace("%type%", type.name()).replace("%mine%", this.name));
	}

	public void despawnHolograms() {
		if (this.blocksMinedHologram != null) {
			this.blocksMinedHologram.despawn();
		}
		if (this.blocksLeftHologram != null) {
			this.blocksLeftHologram.despawn();
		}
	}

	private void subscribeEvents() {
		Events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR)
				.filter(e -> isInMine(e.getBlock().getLocation()) && !e.isCancelled())
				.handler(e -> this.handleBlockBreak(Arrays.asList(e.getBlock()))).bindWith(this.manager.getPlugin().getCore());
	}

	public void updateCurrentBlocks() {
		this.currentBlocks = this.calculateCurrentBlocks();
	}

	public void increaseEffect(PotionEffectType type) {
		this.mineEffects.put(type, this.mineEffects.getOrDefault(type, 0) + 1);
	}

	public PotionEffect getEffect(PotionEffectType type) {
		if (this.mineEffects.containsKey(type)) {
			return new PotionEffect(type, 100, this.mineEffects.get(type));
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
		return player.hasPermission("ultraprison.mines.tp." + this.name);
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
		private Map<PotionEffectType, Integer> mineEffects;

		public Builder() {

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

		public Builder mineEffects(Map<PotionEffectType, Integer> mineEffects) {
			this.mineEffects = mineEffects;
			return this;
		}

		public Mine build() {
			return new Mine(UltraPrisonMines.getInstance().getManager(), this.name, this.mineRegion, this.teleportLocation, this.blockPalette, this.resetType, this.resetPercentage, this.broadcastReset, this.blocksLeftHologram, this.blocksMinedHologram, this.mineEffects);
		}

	}
}
