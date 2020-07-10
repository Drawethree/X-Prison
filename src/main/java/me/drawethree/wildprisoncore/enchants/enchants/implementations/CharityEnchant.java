package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CharityEnchant extends WildPrisonEnchantment {

    private final double chance;
    /*
    private final long minAmount;
    private final long maxAmount;
    */

    public CharityEnchant(WildPrisonEnchants instance) {
        super(instance, 11);
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
        /*
        this.minAmount = instance.getConfig().get().getLong("enchants." + id + ".Min-Money");
        this.maxAmount = instance.getConfig().get().getLong("enchants." + id + ".Max-Money");
         */
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        //double chance = enchantLevel < 25 ? 0.000025 : enchantLevel < 50 ? 0.000022 : enchantLevel < 75 ? 0.00002 : enchantLevel < 100 ? 0.000018 : 0.0000167;

        if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
            double selfAmount = enchantLevel < 25 ? 100000000000000000.0 : enchantLevel < 50 ? 250000000000000000.0 : enchantLevel < 75 ? 500000000000000000.0 : enchantLevel < 100 ? 750000000000000000.0 : 1000000000000000000.0;
            double othersAmount = selfAmount / 10;

            for (Player p : Players.all()) {
                if (p.equals(e.getPlayer())) {
                    plugin.getCore().getEconomy().depositPlayer(p, selfAmount);
                    p.sendMessage(plugin.getMessage("charity_your").replace("%amount%", String.format("%,.0f", selfAmount)));
                } else {
                    plugin.getCore().getEconomy().depositPlayer(p, othersAmount);
                    p.sendMessage(plugin.getMessage("charity_other").replace("%amount%", String.format("%,.0f", othersAmount)).replace("%player%", e.getPlayer().getName()));
                }
            }
        }
    }
}
