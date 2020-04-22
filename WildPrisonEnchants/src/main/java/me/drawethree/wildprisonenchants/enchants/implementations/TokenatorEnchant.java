package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class TokenatorEnchant extends WildPrisonEnchantment {
    public TokenatorEnchant(WildPrisonEnchants instance) {
        super(instance,14);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        
    }
}
