package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class SmasherEnchant extends WildPrisonEnchantment {

    private double chance;

    public SmasherEnchant(WildPrisonEnchants plugin) {
        super(plugin, 1);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
