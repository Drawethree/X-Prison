package dev.drawethree.xprison.enchants.utils;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.model.impl.FortuneEnchant;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public final class EnchantUtils {

    private EnchantUtils() {
        throw new UnsupportedOperationException("Cannot instantiate.");
    }

    public static int getFortuneBlockCount(ItemStack pickaxe, Block block) {
        if (FortuneEnchant.isBlockBlacklisted(block)) {
            return 1;
        }
        return getItemFortuneLevel(pickaxe) + 1;
    }

    public static int getItemFortuneLevel(ItemStack item) {
        if (item == null) {
            return 0;
        }
        XPrisonEnchantment fortuneEnchant = XPrisonEnchants.getInstance().getEnchantsRepository().getEnchantById(3);

        if (fortuneEnchant == null || !fortuneEnchant.isEnabled()) {
            return 0;
        }

        return XPrisonEnchants.getInstance().getEnchantsManager().getEnchantLevel(item, fortuneEnchant);
    }
}
