package dev.drawethree.xprison.mines.migration.model.impl;

import dev.drawethree.xprison.mines.migration.model.MinesMigration;
import dev.drawethree.xprison.mines.model.mine.BlockPalette;
import dev.drawethree.xprison.mines.model.mine.reset.ResetType;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import me.jet315.prisonmines.JetsPrisonMines;
import me.jet315.prisonmines.JetsPrisonMinesAPI;
import me.jet315.prisonmines.mine.Mine;
import me.jet315.prisonmines.mine.blocks.MineBlock;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JetsPrisonMinesMigration extends MinesMigration<Mine> {

	private final JetsPrisonMinesAPI api;

	public JetsPrisonMinesMigration() {
		super("JetsPrisonMines");
		this.api = ((JetsPrisonMines) Bukkit.getPluginManager().getPlugin(this.fromPlugin)).getAPI();
	}

	@Override
	protected List<Mine> getMinesToMigrate() {
		return new ArrayList<>(this.api.getMines());
	}

	@Override
	public dev.drawethree.xprison.mines.model.mine.Mine migrate(Mine mine) {
		dev.drawethree.xprison.mines.model.mine.Mine.Builder builder = new dev.drawethree.xprison.mines.model.mine.Mine.Builder();

		//Name
		String name = mine.getCustomName();
		builder.name(name);

		//Region
		Position minPoint = Position.of(mine.getMineRegion().getMinPoint());
		Position maxPoint = Position.of(mine.getMineRegion().getMaxPoint());
		Region region = minPoint.regionWith(maxPoint);
		builder.region(region);

		//Teleport location
		if (mine.getSpawnLocation() != null) {
			Point teleportLocation = Point.of(mine.getSpawnLocation());
			builder.teleportLocation(teleportLocation);
		}

		//Block palette
		BlockPalette blockPalette = new BlockPalette();

		for (MineBlock block : mine.getBlockManager().getMineBlocks()) {
			float chance = block.getChanceOfSpawning();
			CompMaterial material = CompMaterial.fromItem(block.getItem());
			blockPalette.addToPalette(material, chance);
		}

		builder.blockPalette(blockPalette);

		//Reset percentage
		int resetPercentage = mine.getResetManager().getPercentageReset();
		builder.resetPercentage(resetPercentage);

		//Reset time (minutes)
		int resetTime = mine.getResetManager().getMineResetTime();
		builder.timedReset(resetTime);

		//Reset messages
		boolean broadcastReset = mine.getResetManager().isUseMessages();
		builder.broadcastReset(broadcastReset);

		//Reset type
		ResetType resetType = null;
		switch (mine.getResetManager().getResetType()) {
			case GRADUAL:
				resetType = ResetType.GRADUAL;
				break;
			case INSTANT:
				resetType = ResetType.INSTANT;
				break;
		}
		builder.resetType(resetType);

		//Mine effects
		Map<PotionEffectType, Integer> mineEffects = new HashMap<>();

		for (PotionEffect effect : mine.getEffectsManager().getPotionEffects()) {
			mineEffects.put(effect.getType(), effect.getAmplifier());
		}
		builder.mineEffects(mineEffects);

		return builder.build();
	}

	@Override
	public boolean preValidateMigration() {
		return this.api != null;
	}
}
