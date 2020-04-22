package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class FlyEnchantEnchant extends WildPrisonEnchantment {
    public FlyEnchantEnchant(WildPrisonEnchants instance) {
        super(instance, 8);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
