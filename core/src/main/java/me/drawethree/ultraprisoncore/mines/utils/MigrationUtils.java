package me.drawethree.ultraprisoncore.mines.utils;

import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.mines.model.mine.reset.ResetType;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.jet315.prisonmines.mine.blocks.MineBlock;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.serialize.Region;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class MigrationUtils {

	/**
	 * Migrate from JetsPrisonMines
	 *
	 * @param mine me.jet315.prisonmines.mine.Mine
	 * @return me.drawethree.ultraprisoncore.mines.model.mine.Mine
	 */
	public static Mine migrate(me.jet315.prisonmines.mine.Mine mine) {
		Mine.Builder builder = new Mine.Builder();

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
}
