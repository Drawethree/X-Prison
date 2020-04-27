package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class FlyEnchantEnchant extends WildPrisonEnchantment {
    public FlyEnchantEnchant(WildPrisonEnchants instance) {
        super(instance, 8);
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {
        if (level == 0) {
            this.onUnequip(p,pickAxe,level);
            return;
        }
        p.setAllowFlight(true);
        p.setFlying(true);
    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {
        p.setAllowFlight(false);
        p.setFlying(false);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
