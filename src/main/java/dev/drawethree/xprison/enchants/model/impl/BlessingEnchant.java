package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.api.enums.ReceiveCause;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class BlessingEnchant extends XPrisonEnchantment {

    private double chance;
    private String amountToGiveExpression;

    public BlessingEnchant(XPrisonEnchants instance) {
        super(instance, 13);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!this.plugin.getCore().isModuleEnabled(XPrisonTokens.MODULE_NAME)) {
            return;
        }

        double chance = getChanceToTrigger(enchantLevel);

        if (chance < ThreadLocalRandom.current().nextDouble(100)) {
            return;
        }

        long amount = (long) createExpression(enchantLevel).evaluate();

        for (Player p : Players.all()) {
            plugin.getCore().getTokens().getTokensManager().giveTokens(p, amount, null, ReceiveCause.MINING_OTHERS);

            if (!this.isMessagesEnabled()) {
                continue;
            }

            if (p.equals(e.getPlayer())) {
                PlayerUtils.sendMessage(p, plugin.getEnchantsConfig().getMessage("blessing_your").replace("%amount%", String.format("%,d", amount)));
            } else {
                PlayerUtils.sendMessage(p, plugin.getEnchantsConfig().getMessage("blessing_other").replace("%amount%", String.format("%,d", amount)).replace("%player%", e.getPlayer().getName()));
            }
        }
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void reload() {
        super.reload();
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
        this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
    }

    private Expression createExpression(int level) {
        return new ExpressionBuilder(this.amountToGiveExpression)
                .variables("level")
                .build()
                .setVariable("level", level);
    }

}
