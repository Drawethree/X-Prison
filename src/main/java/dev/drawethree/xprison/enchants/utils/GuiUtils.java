package dev.drawethree.xprison.enchants.utils;

import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.enchants.model.RequiresPickaxeLevel;
import dev.drawethree.xprison.api.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.utils.economy.EconomyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GuiUtils {

    private GuiUtils() {
        throw new UnsupportedOperationException("Cannot instantiate.");
    }

    public static List<String> translateGuiLore(XPrisonEnchantment enchantment, List<String> guiItemLore,
                                                int currentLevel) {
        List<String> newList = new ArrayList<>();
        for (String s : guiItemLore) {
            if (s.contains("%description%")) {
                newList.addAll(enchantment.getGuiProperties().getGuiDescription());
                continue;
            }
            newList.add(s
                    .replace("%refund%", String.format("%,d", EnchantUtils.getRefundForLevel(enchantment, currentLevel)))
                    .replace("%currency%", StringUtils.capitalize(EconomyUtils.getCurrencyName(enchantment.getCurrencyType())))
                    .replace("%cost%", String.format("%,d", enchantment.getBaseCost() + (enchantment.getIncreaseCost() * currentLevel)))
                    .replace("%max_level%", enchantment.getMaxLevel() == Integer.MAX_VALUE ? "Unlimited" : String.format("%,d", enchantment.getMaxLevel()))
                    .replace("%chance%", String.format("%,.2f", enchantment instanceof ChanceBasedEnchant ? ((ChanceBasedEnchant) enchantment).getChanceToTrigger(currentLevel) : 100.00F))
                    .replace("%current_level%", String.format("%,d", currentLevel))
                    .replace("%pickaxe_level%", String.format("%,d", enchantment instanceof RequiresPickaxeLevel ? ((RequiresPickaxeLevel) enchantment).getRequiredPickaxeLevel() : 0)));
        }
        return newList;
    }
}
