package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.shared.currency.enums.ReceiveCause;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.expression.ExpressionUtils;
import dev.drawethree.xprison.utils.json.JsonUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
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

        long amount = (long) ExpressionUtils.createExpressionWithSingleVariable(amountToGiveExpression,"level", enchantLevel).evaluate();

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
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
        this.amountToGiveExpression = JsonUtils.getString(config,"amountToGive","");
        this.messagesEnabled = JsonUtils.getBoolean(config,"messagesEnabled",false);
    }

}
