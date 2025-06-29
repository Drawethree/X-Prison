package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.shared.currency.enums.ReceiveCause;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.event.block.BlockBreakEvent;

public final class TokenatorEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private String amountToGiveExpression;


    public TokenatorEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (!getCore().isModuleEnabled(XPrisonTokens.MODULE_NAME)) {
            return;
        }

        long randAmount = (long) createExpression(enchantLevel).evaluate();
        getCore().getTokens().getTokensManager().giveTokens(e.getPlayer(), randAmount, null, ReceiveCause.MINING);
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
