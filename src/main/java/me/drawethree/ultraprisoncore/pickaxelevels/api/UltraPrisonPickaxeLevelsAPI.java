package me.drawethree.ultraprisoncore.pickaxelevels.api;

import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import org.bukkit.inventory.ItemStack;

public interface UltraPrisonPickaxeLevelsAPI {

	/**
	 * Method to get PickaxeLevel of itemstack
	 *
	 * @param item ItemStack
	 * @return PickaxeLevel.class
	 */
	PickaxeLevel getPickaxeLevel(ItemStack item);
}
