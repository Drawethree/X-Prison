package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.utils.expression.ExpressionUtils;
import dev.drawethree.xprison.utils.json.JsonUtils;
import org.bukkit.event.block.BlockBreakEvent;


public final class GangValueFinderEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private String amountToGiveExpression;

    public GangValueFinderEnchant() {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!getCore().isModuleEnabled(XPrisonGangs.MODULE_NAME)) {
            return;
        }

        int amount = (int) ExpressionUtils.createExpressionWithSingleVariable(amountToGiveExpression,"level", enchantLevel).evaluate();
        getCore().getGangs().getGangsManager().getPlayerGang(e.getPlayer()).ifPresent(gang -> gang.setValue(gang.getValue() + amount));

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
        this.amountToGiveExpression = JsonUtils.getString(config,"amountToGive","");
    }
}