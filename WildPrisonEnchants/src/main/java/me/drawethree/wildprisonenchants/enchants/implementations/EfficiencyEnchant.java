package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class EfficiencyEnchant extends WildPrisonEnchantment {
    public EfficiencyEnchant(WildPrisonEnchants instance) {
        super(instance,1);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
