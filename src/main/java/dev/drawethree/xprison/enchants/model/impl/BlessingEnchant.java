package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.shared.currency.enums.ReceiveCause;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlessingEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;
    private String amountToGiveExpression;
    private boolean messagesEnabled;

    public BlessingEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!getCore().isModuleEnabled(XPrisonTokens.MODULE_NAME)) {
            return;
        }

        long amount = (long) createExpression(enchantLevel).evaluate();

        for (Player p : Players.all()) {
            getCore().getTokens().getTokensManager().giveTokens(p, amount, null, ReceiveCause.MINING_OTHERS);

            if (!messagesEnabled) {
                continue;
            }

            if (p.equals(e.getPlayer())) {
                PlayerUtils.sendMessage(p, getEnchants().getEnchantsConfig().getMessage("blessing_your").replace("%amount%", String.format("%,d", amount)));
            } else {
                PlayerUtils.sendMessage(p, getEnchants().getEnchantsConfig().getMessage("blessing_other").replace("%amount%", String.format("%,d", amount)).replace("%player%", e.getPlayer().getName()));
            }
        }
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = config.get("chance").getAsDouble();
        this.amountToGiveExpression = config.get("amountToGive").getAsString();
        this.messagesEnabled = config.get("messagesEnabled").getAsBoolean();
    }

    private Expression createExpression(int level) {
        return new ExpressionBuilder(this.amountToGiveExpression)
                .variables("level")
                .build()
                .setVariable("level", level);
    }

}
