package dev.drawethree.ultraprisoncore.enchants.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class EnchantUtils {

    private EnchantUtils() {
        throw new UnsupportedOperationException("Cannot instantiate.");
    }

    public static int getItemFortuneLevel(ItemStack item) {
        if (item == null) {
            return 0;
        }
        return item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
    }
}
