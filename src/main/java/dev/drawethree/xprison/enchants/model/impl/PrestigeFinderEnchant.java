package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;

public final class PrestigeFinderEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private String commandToExecute;
    private String amountToGiveExpression;

    public PrestigeFinderEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        int levels = (int) createExpression(enchantLevel).evaluate();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute.replace("%player%", e.getPlayer().getName()).replace("%amount%", String.valueOf(levels)));
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = config.get("chance").getAsDouble();
        this.amountToGiveExpression = config.get("amountToGive").getAsString();
        this.commandToExecute = config.get("command").getAsString();
    }

    private Expression createExpression(int level) {
        return new ExpressionBuilder(this.amountToGiveExpression)
                .variables("level")
                .build()
                .setVariable("level", level);
    }
}
