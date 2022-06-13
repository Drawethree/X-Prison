package dev.drawethree.ultraprisoncore.enchants.utils;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class EnchantUtils {

    public static int getItemFortuneLevel(ItemStack item) {
        return UltraPrisonCore.getInstance().isModuleEnabled(UltraPrisonEnchants.MODULE_NAME) ?
                UltraPrisonCore.getInstance().getEnchants().getApi().getEnchantLevel(item, 3) :
                item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
    }
}
