package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.utils.expression.ExpressionUtils;
import dev.drawethree.xprison.utils.json.JsonUtils;
import org.bukkit.event.block.BlockBreakEvent;


public final class SalaryEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private String amountToGiveExpression;

    public SalaryEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        double randAmount = ExpressionUtils.createExpressionWithSingleVariable(amountToGiveExpression,"level", enchantLevel).evaluate();

        getCore().getEconomy().depositPlayer(e.getPlayer(), randAmount);

        if (getEnchants().isAutoSellModuleEnabled()) {
            getCore().getAutoSell().getManager().addToCurrentEarnings(e.getPlayer(), randAmount);
        }

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return this.chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
        this.amountToGiveExpression = JsonUtils.getString(config,"amountToGive", "");
    }
}
