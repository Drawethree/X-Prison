package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class ExplosiveEnchant extends WildPrisonEnchantment {
    public ExplosiveEnchant(WildPrisonEnchants instance) {
        super(instance,9);
    }



    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
