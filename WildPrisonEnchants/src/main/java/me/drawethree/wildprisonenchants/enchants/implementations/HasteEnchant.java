package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class HasteEnchant extends WildPrisonEnchantment {

    private final double chance;

    public HasteEnchant(WildPrisonEnchants instance) {
        super(instance,4);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
