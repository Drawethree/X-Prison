package dev.drawethree.xprison.enchants.utils;

import com.saicone.rtag.RtagItem;
import com.saicone.rtag.util.ServerInstance;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.model.impl.FortuneEnchant;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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

    public static ItemStack setUnbreakable(ItemStack item, boolean bool) {
        if (ServerInstance.verNumber >= 11) {
            final ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setUnbreakable(bool);
                item.setItemMeta(meta);
                return item;
            }
        }
        return RtagItem.edit(item, tag -> {
            tag.setUnbreakable(bool);
        });
    }

    public static boolean isUnbreakable(ItemStack item) {
        return isUnbreakable(item, item.getItemMeta());
    }

    public static boolean isUnbreakable(ItemStack item, ItemMeta meta) {
        if (item.getType().isBlock() || item.getType().getMaxDurability() < 1) {
            return true;
        }
        if (ServerInstance.verNumber >= 11) {
            return meta.isUnbreakable();
        } else {
            return new RtagItem(item).isUnbreakable();
        }
    }
}
