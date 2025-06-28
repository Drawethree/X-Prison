package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentAbstract;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;

public final class PrestigeFinderEnchant extends XPrisonEnchantmentAbstract implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private String commandToExecute;
    private String amountToGiveExpression;

    public PrestigeFinderEnchant(XPrisonEnchants instance) {
        super(instance, 16);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandToExecute = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Command");
        this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
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
    public void reload() {
        super.reload();
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.commandToExecute = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Command");
        this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
    }

    private Expression createExpression(int level) {
        return new ExpressionBuilder(this.amountToGiveExpression)
                .variables("level")
                .build()
                .setVariable("level", level);
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }
}
