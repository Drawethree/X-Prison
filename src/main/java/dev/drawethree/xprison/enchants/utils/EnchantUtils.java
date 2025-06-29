package dev.drawethree.xprison.enchants.utils;

import com.saicone.rtag.util.ServerInstance;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.enchants.model.RefundableEnchant;
import dev.drawethree.xprison.api.enchants.model.RequiresPickaxeLevel;
import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.impl.FortuneEnchant;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevelImpl;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public final class EnchantUtils {

    private EnchantUtils() {
        throw new UnsupportedOperationException("Cannot instantiate.");
    }

    public static long getCostOfLevel(XPrisonEnchantment enchantment, int level) {
        return (enchantment.getBaseCost() + (enchantment.getIncreaseCost() * (level - 1)));
    }

    public static boolean canBeBought(XPrisonEnchantment enchantment, ItemStack pickAxe) {
        if (!XPrison.getInstance().isModuleEnabled(XPrisonPickaxeLevels.MODULE_NAME)) {
            return true;
        }
        if (!(enchantment instanceof RequiresPickaxeLevel)) {
            return true;
        }
        Optional<PickaxeLevelImpl> pickaxeLevelOptional = XPrison.getInstance().getPickaxeLevels().getPickaxeLevelsManager().getPickaxeLevel(pickAxe);
        return pickaxeLevelOptional.map(level -> level.getLevel() >= ((RequiresPickaxeLevel) enchantment).getRequiredPickaxeLevel()).orElse(true);
    }

    public static long getRefundForLevel(XPrisonEnchantment enchantment, int level) {
        if (!(enchantment instanceof RefundableEnchant)) {
            return 0;
        }
        return (long) (getCostOfLevel(enchantment, level) * (((RefundableEnchant) enchantment).getRefundPercentage() / 100.0));
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

    public static int getDurability(ItemStack item) {
        return getDurability(item, item.getItemMeta());
    }

    public static int getDurability(ItemStack item, ItemMeta meta) {
        if (ServerInstance.isLegacy) {
            return item.getDurability();
        } else if (meta instanceof Damageable) {
            return ((Damageable) meta).getDamage();
        }
        return 0;
    }


}
