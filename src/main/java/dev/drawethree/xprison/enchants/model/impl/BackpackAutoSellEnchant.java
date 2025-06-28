package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultrabackpacks.api.exception.BackpackNotFoundException;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.api.enchants.model.BlockBreakEnchant;
import dev.drawethree.xprison.api.enchants.model.ChanceBasedEnchant;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentAbstract;
import org.bukkit.event.block.BlockBreakEvent;

public final class BackpackAutoSellEnchant extends XPrisonEnchantmentAbstract implements BlockBreakEnchant, ChanceBasedEnchant {

    private double chance;

    public BackpackAutoSellEnchant(XPrisonEnchants instance) {
        super(instance, 19);
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public String getAuthor() {
        return "Drawethree";
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (!XPrison.getInstance().isUltraBackpacksEnabled()) {
            return;
        }

        try {
            UltraBackpacksAPI.sellBackpack(e.getPlayer(), true);
        } catch (BackpackNotFoundException ignored) {
            this.plugin.getCore().debug("BackpackAutoSellEnchant::onBlockBreak > Player " + e.getPlayer().getName() + " does not have backpack.", this.plugin);
        }

    }

    @Override
    public double getChanceToTrigger(int enchantLevel) {
        return this.chance * enchantLevel;
    }

    @Override
    public void reload() {
        super.reload();
        this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
    }
}
