package dev.drawethree.ultraprisoncore.enchants.api;

import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface UltraPrisonEnchantsAPI {


	/**
	 * Method to get all custom enchants applied on specific ItemStack
	 *
	 * @param itemStack ItemStack
	 * @return
	 */
	Map<UltraPrisonEnchantment, Integer> getEnchants(ItemStack itemStack);

	/**
	 * Method to check if item has specific enchant
	 *
	 * @param item        {@link ItemStack}
	 * @param enchantment {@link UltraPrisonEnchantment}
	 * @return true if item has enchant
	 */
	boolean hasEnchant(ItemStack item, UltraPrisonEnchantment enchantment);

	/**
	 * Method to get enchant level of specific ItemStack
	 *
	 * @param item        ItemStack
	 * @param enchantment {@link UltraPrisonEnchantment}
	 * @return 0 if enchant was not found, otherwise level of enchant
	 */
	int getEnchantLevel(ItemStack item, UltraPrisonEnchantment enchantment);

	/**
	 * Method to set enchant with specific level to pickaxe
	 *
	 * @param item        pickaxe
	 * @param enchantment {@link UltraPrisonEnchantment}
	 * @param level       Enchant Level
	 * @return modified ItemStack
	 */
	ItemStack setEnchantLevel(Player player, ItemStack item, UltraPrisonEnchantment enchantment, int level);

	/**
	 * Method to remove enchant from pickaxe
	 *
	 * @param item        ItemStack pickaxe
	 * @param enchantment {@link UltraPrisonEnchantment}
	 * @return modified ItemStack
	 */
	ItemStack removeEnchant(Player player, ItemStack item, UltraPrisonEnchantment enchantment);

	/**
	 * Method to get Enchant by ID
	 *
	 * @param id enchant id
	 * @return UltraPrisonEnchantment
	 */
	UltraPrisonEnchantment getById(int id);

	/**
	 * Method to get Enchant by ID
	 *
	 * @param rawName enchant rawname
	 * @return UltraPrisonEnchantment
	 */
	UltraPrisonEnchantment getByName(String rawName);

}
