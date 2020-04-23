package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HasteEnchant extends WildPrisonEnchantment {

    private final double chance;

    public HasteEnchant(WildPrisonEnchants instance) {
        super(instance,4);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");

    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {
        if (level == 0) {
            this.onUnequip(p,pickAxe,level);
            return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, level-1,true,true),true);
    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {
        p.removePotionEffect(PotionEffectType.FAST_DIGGING);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
