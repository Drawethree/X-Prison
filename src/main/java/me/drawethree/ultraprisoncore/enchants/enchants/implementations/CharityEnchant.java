package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CharityEnchant extends UltraPrisonEnchantment {

    private final double chance;
    /*
    private final long minAmount;
    private final long maxAmount;
    */

    public CharityEnchant(UltraPrisonEnchants instance) {
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
            //double selfAmount = enchantLevel < 25 ? 100000000000000000.0 : enchantLevel < 50 ? 250000000000000000.0 : enchantLevel < 75 ? 500000000000000000.0 : enchantLevel < 100 ? 750000000000000000.0 : 1000000000000000000.0;
            //double othersAmount = selfAmount / 10;

            //boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());
            double selfAmount = this.getSelfAmount(enchantLevel);
            double othersAmount = this.getOthersAmount(enchantLevel);

            for (Player p : Players.all()) {
                if (p.equals(e.getPlayer())) {
                    plugin.getCore().getEconomy().depositPlayer(p, selfAmount);
                    plugin.getCore().getAutoSell().addToCurrentEarnings(p, selfAmount);
                    p.sendMessage(plugin.getMessage("charity_your").replace("%amount%", String.format("%,.0f", selfAmount)));
                } else {
                    if (othersAmount > 0) {
                        plugin.getCore().getEconomy().depositPlayer(p, othersAmount);
                        plugin.getCore().getAutoSell().addToCurrentEarnings(p, othersAmount);
                        p.sendMessage(plugin.getMessage("charity_other").replace("%amount%", String.format("%,.0f", othersAmount)).replace("%player%", e.getPlayer().getName()));
                    }
                }
            }
        }
    }


    public double getSelfAmount(int enchantLevel) {
        if (enchantLevel <= 20) {
            return 35000000000000000.0;
        } else if (enchantLevel <= 30) {
            return 70000000000000000.0;
        } else if (enchantLevel <= 40) {
            return 105000000000000000.0;
        } else if (enchantLevel <= 50) {
            return 140000000000000000.0;
        } else if (enchantLevel <= 60) {
            return 175000000000000000.0;
        } else if (enchantLevel <= 70) {
            return 210000000000000000.0;
        } else if (enchantLevel <= 80) {
            return 245000000000000000.0;
        } else if (enchantLevel <= 90) {
            return 280000000000000000.0;
        } else if (enchantLevel <= 99) {
            return 315000000000000000.0;
        } else {
            return 350000000000000000.0;
        }
    }

    public double getOthersAmount(int enchantLevel) {
        if (enchantLevel <= 20) {
            return 0.0;
        } else if (enchantLevel <= 40) {
            return 3000000000000000.0;
        } else if (enchantLevel <= 60) {
            return 6000000000000000.0;
        } else if (enchantLevel <= 80) {
            return 9000000000000000.0;
        } else if (enchantLevel <= 99) {
            return 12000000000000000.0;
        } else {
            return 15000000000000000.0;
        }
    }
}
