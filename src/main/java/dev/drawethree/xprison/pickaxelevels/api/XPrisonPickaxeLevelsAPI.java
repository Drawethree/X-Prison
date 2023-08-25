package dev.drawethree.xprison.pickaxelevels.api;

import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface XPrisonPickaxeLevelsAPI {

	/**
	 * Method to get PickaxeLevel of itemstack
	 *
	 * @param item ItemStack
	 * @return instance of PickaxeLevel
	 */
	Optional<PickaxeLevel> getPickaxeLevel(ItemStack item);

	/**
	 * Method to get PickaxeLevel of Player
	 *
	 * @param player Player
	 * @return instance of PickaxeLevel
	 */
	Optional<PickaxeLevel> getPickaxeLevel(Player player);

	/**
	 * Method to get PickaxeLevel by specific level
	 *
	 * @param level level
	 * @return instance of PickaxeLevel
	 */
	Optional<PickaxeLevel> getPickaxeLevel(int level);

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

	/**
	 * Method to get progress bar of PickaxeLevel progress for given player
	 *
	 * @param player Player
	 */
	String getProgressBar(Player player);

}
