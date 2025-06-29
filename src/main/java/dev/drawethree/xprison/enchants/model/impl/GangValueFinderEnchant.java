package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
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

        int amount = (int) createExpression(enchantLevel).evaluate();
        getCore().getGangs().getGangsManager().getPlayerGang(e.getPlayer()).ifPresent(gang -> gang.setValue(gang.getValue() + amount));

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = config.get("chance").getAsDouble();
        this.amountToGiveExpression = config.get("amountToGive").getAsString();
    }

    private Expression createExpression(int level) {
        return new ExpressionBuilder(this.amountToGiveExpression)
                .variables("level")
                .build()
                .setVariable("level", level);
    }
}