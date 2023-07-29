package dev.drawethree.xprison.utils.misc;

import dev.drawethree.xprison.utils.compat.CompMaterial;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MaterialUtils {

	public static ItemStack getSmeltedFormAsItemStack(Block block) {
		CompMaterial material = CompMaterial.fromBlock(block);
		switch (material) {
			case STONE:
				return CompMaterial.COBBLESTONE.toItem();
			case COAL_ORE:
				return CompMaterial.COAL.toItem();
			case DIAMOND_ORE:
				return CompMaterial.DIAMOND.toItem();
			case EMERALD_ORE:
				return CompMaterial.EMERALD.toItem();
			case REDSTONE_ORE:
				return CompMaterial.REDSTONE.toItem();
			case GOLD_ORE:
				return CompMaterial.GOLD_INGOT.toItem();
			case IRON_ORE:
				return CompMaterial.IRON_INGOT.toItem();
			case NETHER_QUARTZ_ORE:
				return CompMaterial.QUARTZ.toItem();
			case LAPIS_ORE:
				return CompMaterial.LAPIS_LAZULI.toItem();
			default:
				return material.toItem();
		}
	}

	private MaterialUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
