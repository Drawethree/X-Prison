package dev.drawethree.xprison.utils.misc;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MaterialUtils {

	public static ItemStack getSmeltedFormAsItemStack(Block block) {
		XMaterial material = XMaterial.matchXMaterial(block.getType());
		switch (material) {
			case STONE:
				return XMaterial.COBBLESTONE.parseItem();
			case COAL_ORE:
				return XMaterial.COAL.parseItem();
			case DIAMOND_ORE:
				return XMaterial.DIAMOND.parseItem();
			case EMERALD_ORE:
				return XMaterial.EMERALD.parseItem();
			case REDSTONE_ORE:
				return XMaterial.REDSTONE.parseItem();
			case GOLD_ORE:
				return XMaterial.GOLD_INGOT.parseItem();
			case IRON_ORE:
				return XMaterial.IRON_INGOT.parseItem();
			case NETHER_QUARTZ_ORE:
				return XMaterial.QUARTZ.parseItem();
			case LAPIS_ORE:
				return XMaterial.LAPIS_LAZULI.parseItem();
			default:
				return material.parseItem();
		}
	}

	private MaterialUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
