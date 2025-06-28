package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;

import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentAbstract;
import dev.drawethree.xprison.utils.misc.RegionUtils;
import org.bukkit.event.block.BlockBreakEvent;


public final class AutoSellEnchant extends XPrisonEnchantmentAbstract implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;

    public AutoSellEnchant(XPrisonEnchants instance) {
        super(instance, 19);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!this.plugin.getCore().isModuleEnabled(XPrisonAutoSell.MODULE_NAME)) {
            return;
        }

        this.plugin.getCore().getAutoSell().getManager().sellAll(e.getPlayer(), RegionUtils.getRegionWithHighestPriority(e.getPlayer().getLocation()));

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return chance * enchantLevel;
    }

    @Override
    public void reload() {
        super.reload();
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
    }
}
