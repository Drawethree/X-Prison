package me.drawethree.ultraprisoncore.utils.misc;

import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import org.bukkit.Material;

public class MaterialUtils {

	public static Material getSmeltedForm(Material m) {
		CompMaterial material = CompMaterial.fromMaterial(m);
		switch (material) {
			case STONE:
				return Material.COBBLESTONE;
			case COAL_ORE:
				return Material.COAL;
			case DIAMOND_ORE:
				return Material.DIAMOND;
			case EMERALD_ORE:
				return Material.EMERALD;
			case REDSTONE_ORE:
				return Material.REDSTONE;
			case GOLD_ORE:
				return Material.GOLD_INGOT;
			case IRON_ORE:
				return Material.IRON_INGOT;
			case NETHER_QUARTZ_ORE:
				return Material.QUARTZ;
			default:
				return m;
		}
	}

	private MaterialUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
