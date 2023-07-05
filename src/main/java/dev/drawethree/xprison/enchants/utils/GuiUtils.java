package dev.drawethree.xprison.enchants.utils;

import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;

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
                newList.addAll(enchantment.getDescription());
                continue;
            }
            newList.add(s
                    .replace("%refund%", String.format("%,d", enchantment.getRefundForLevel(currentLevel)))
                    .replace("%cost%", String.format("%,d", enchantment.getCost() + (enchantment.getIncreaseCost() * currentLevel)))
                    .replace("%max_level%", enchantment.getMaxLevel() == Integer.MAX_VALUE ? "Unlimited" : String.format("%,d", enchantment.getMaxLevel()))
                    .replace("%chance%", String.format("%,.2f", enchantment.getChanceToTrigger(currentLevel)))
                    .replace("%current_level%", String.format("%,d", currentLevel))
                    .replace("%pickaxe_level%", String.format("%,d", enchantment.getRequiredPickaxeLevel())));
        }
        return newList;
    }
}
