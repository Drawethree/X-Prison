package dev.drawethree.xprison.enchants.api;

import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface XPrisonEnchantsAPI {


	/**
	 * Method to get all custom enchants applied on specific ItemStack
	 *
	 * @param itemStack ItemStack
	 * @return
	 */
	Map<XPrisonEnchantment, Integer> getEnchants(ItemStack itemStack);

	/**
	 * Method to check if item has specific enchant
	 *
	 * @param item        {@link ItemStack}
	 * @param enchantment {@link XPrisonEnchantment}
	 * @return true if item has enchant
	 */
	boolean hasEnchant(ItemStack item, XPrisonEnchantment enchantment);

	/**
	 * Method to get enchant level of specific ItemStack
	 *
	 * @param item        ItemStack
	 * @param enchantment {@link XPrisonEnchantment}
	 * @return 0 if enchant was not found, otherwise level of enchant
	 */
	int getEnchantLevel(ItemStack item, XPrisonEnchantment enchantment);

	/**
	 * Method to set enchant with specific level to pickaxe
	 *
	 * @param item        pickaxe
	 * @param enchantment {@link XPrisonEnchantment}
	 * @param level       Enchant Level
	 * @return modified ItemStack
	 */
	ItemStack setEnchantLevel(Player player, ItemStack item, XPrisonEnchantment enchantment, int level);

	/**
	 * Method to remove enchant from pickaxe
	 *
	 * @param item        ItemStack pickaxe
	 * @param enchantment {@link XPrisonEnchantment}
	 * @return modified ItemStack
	 */
	ItemStack removeEnchant(Player player, ItemStack item, XPrisonEnchantment enchantment);

	/**
	 * Method to get Enchant by ID
	 *
	 * @param id enchant id
	 * @return XPrisonEnchantment
	 */
	XPrisonEnchantment getById(int id);

	/**
	 * Method to get Enchant by ID
	 *
	 * @param rawName enchant rawname
	 * @return XPrisonEnchantment
	 */
	XPrisonEnchantment getByName(String rawName);

	/**
	 * Registers a specific {@link XPrisonEnchantment}
	 *
	 * @param enchantment
	 * @return
	 */
	boolean registerEnchant(XPrisonEnchantment enchantment);

	/**
	 * Unregisters a specific {@link XPrisonEnchantment}
	 *
	 * @param enchantment
	 * @return
	 */
	boolean unregisterEnchant(XPrisonEnchantment enchantment);

}
