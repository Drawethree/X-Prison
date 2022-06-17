package dev.drawethree.ultraprisoncore.pickaxelevels.api;

import dev.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface UltraPrisonPickaxeLevelsAPI {

	/**
	 * Method to get PickaxeLevel of itemstack
	 *
	 * @param item ItemStack
	 * @return instance of PickaxeLevel
	 */
	PickaxeLevel getPickaxeLevel(ItemStack item);

	/**
	 * Method to get PickaxeLevel of Player
	 *
	 * @param player Player
	 * @return instance of PickaxeLevel
	 */
	PickaxeLevel getPickaxeLevel(Player player);

	/**
	 * Method to get PickaxeLevel by specific level
	 *
	 * @param level level
	 * @return instance of PickaxeLevel
	 */
	PickaxeLevel getPickaxeLevel(int level);

	/**
	 * Method to set PickaxeLevel of itemstack
	 *
	 * @param player Player
	 * @param item   ItemStack to change
	 * @param level  PickaxeLevel to set
	 */
	void setPickaxeLevel(Player player, ItemStack item, PickaxeLevel level);

	/**
	 * Method to set PickaxeLevel of itemstack
	 *
	 * @param player Player
	 * @param item   ItemStack to change
	 * @param level  level to set
	 */
	void setPickaxeLevel(Player player, ItemStack item, int level);
}
