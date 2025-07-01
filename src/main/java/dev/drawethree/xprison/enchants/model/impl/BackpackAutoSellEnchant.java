package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultrabackpacks.api.exception.BackpackNotFoundException;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.utils.json.JsonUtils;
import org.bukkit.event.block.BlockBreakEvent;

public final class BackpackAutoSellEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;

    public BackpackAutoSellEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!XPrison.getInstance().isUltraBackpacksEnabled()) {
            return;
        }

        try {
            UltraBackpacksAPI.sellBackpack(e.getPlayer(), true);
        } catch (BackpackNotFoundException ignored) {
            getCore().debug("BackpackAutoSellEnchant::onBlockBreak > Player " + e.getPlayer().getName() + " does not have backpack.", getEnchants());
        }

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return this.chance * enchantLevel;
    }

    @Override
    public void loadCustomProperties(JsonObject config) {
        this.chance = JsonUtils.getDouble(config, "chance", 0.0);
    }
}
