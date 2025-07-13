package dev.drawethree.xprison.enchants.model;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interface representing enchantments that have effects when equipped or unequipped.
 */
public interface EquipabbleEnchantment {

    /**
     * Called when a pickaxe with this enchantment is equipped by a player.
     *
     * @param player  the player equipping the pickaxe
     * @param pickaxe the pickaxe item being equipped
     * @param level   the level of the enchantment on the pickaxe
     */
    void onEquip(Player player, ItemStack pickaxe, int level);

    /**
     * Called when a pickaxe with this enchantment is unequipped by a player.
     *
     * @param player  the player unequipping the pickaxe
     * @param pickaxe the pickaxe item being unequipped
     * @param level   the level of the enchantment on the pickaxe
     */
    void onUnequip(Player player, ItemStack pickaxe, int level);
}
