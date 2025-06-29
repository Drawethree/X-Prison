package dev.drawethree.xprison.enchants.model.impl;

import com.google.gson.JsonObject;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.api.enchants.model.RequiresPickaxeLevel;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import dev.drawethree.xprison.utils.misc.RegionUtils;
import org.bukkit.event.block.BlockBreakEvent;


public final class AutoSellEnchant extends XPrisonEnchantmentBaseCore implements BlockBreakEnchant, ChanceBasedEnchant, RequiresPickaxeLevel {

    private double chance;
    private int requiredPickaxeLevel;

    public AutoSellEnchant() {
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!getCore().isModuleEnabled(XPrisonAutoSell.MODULE_NAME)) {
            return;
        }

        getCore().getAutoSell().getManager().sellAll(e.getPlayer(), RegionUtils.getRegionWithHighestPriority(e.getPlayer().getLocation()));
    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public int getRequiredPickaxeLevel() {
        return requiredPickaxeLevel;
    }

    @Override
    protected void loadCustomProperties(JsonObject config) {
        this.chance = config.get("chance").getAsDouble();
        this.requiredPickaxeLevel = config.get("pickaxeLevelRequired").getAsInt();
    }
}
