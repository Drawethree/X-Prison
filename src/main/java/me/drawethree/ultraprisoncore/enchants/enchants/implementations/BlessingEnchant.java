package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class BlessingEnchant extends UltraPrisonEnchantment {
    private final double chance;
    /*
    private final long minAmount;
    private final long maxAmount;
    */

    public BlessingEnchant(UltraPrisonEnchants instance) {
        super(instance, 13);
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");

        /*
        this.minAmount = instance.getConfig().get().getLong("enchants." + id + ".Min-Tokens");
        this.maxAmount = instance.getConfig().get().getLong("enchants." + id + ".Max-Tokens");

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

        //double chance = enchantLevel < 25 ? 0.00002 : enchantLevel < 50 ? 0.0000167 : enchantLevel < 75 ? 0.0000143 : enchantLevel < 100 ? 0.0000125 : 0.00001;

        if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {

            //long selfAmount = enchantLevel < 25 ? 2000000 : enchantLevel < 50 ? 4000000 : enchantLevel < 75 ? 6000000 : enchantLevel < 100 ? 8000000 : 10000000;
            //long othersAmount = selfAmount / 10;

            //boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());
            long selfAmount = this.getSelfAmount(enchantLevel);
            long othersAmount = this.getOthersAmount(enchantLevel);

            for (Player p : Players.all()) {
                if (p.equals(e.getPlayer())) {
                    plugin.getCore().getTokens().getTokensManager().giveTokens(p, selfAmount, null, ReceiveCause.MINING);
                    p.sendMessage(plugin.getMessage("blessing_your").replace("%amount%", String.format("%,d", selfAmount)));
                } else {
                    if (othersAmount > 0) {
                        plugin.getCore().getTokens().getTokensManager().giveTokens(p, othersAmount, null, ReceiveCause.MINING);
                        p.sendMessage(plugin.getMessage("blessing_other").replace("%amount%", String.format("%,d", othersAmount)).replace("%player%", e.getPlayer().getName()));
                    }
                }
            }
        }
    }

    public long getSelfAmount(int enchantLevel) {
        if (enchantLevel <= 20) {
            return 700000;
        } else if (enchantLevel <= 30) {
            return 1400000;
        } else if (enchantLevel <= 40) {
            return 2100000;
        } else if (enchantLevel <= 50) {
            return 2800000;
        } else if (enchantLevel <= 60) {
            return 3500000;
        } else if (enchantLevel <= 70) {
            return 4200000;
        } else if (enchantLevel <= 80) {
            return 4900000;
        } else if (enchantLevel <= 90) {
            return 5600000;
        } else if (enchantLevel <= 99) {
            return 6300000;
        } else {
            return 7000000;
        }
    }

    public long getOthersAmount(int enchantLevel) {
        if (enchantLevel <= 10) {
            return 0;
        } else if (enchantLevel <= 20) {
            return 35000;
        } else if (enchantLevel <= 30) {
            return 70000;
        } else if (enchantLevel <= 40) {
            return 105000;
        } else if (enchantLevel <= 50) {
            return 140000;
        } else if (enchantLevel <= 60) {
            return 175000;
        } else if (enchantLevel <= 70) {
            return 210000;
        } else if (enchantLevel <= 80) {
            return 245000;
        } else if (enchantLevel <= 90) {
            return 280000;
        } else if (enchantLevel <= 99) {
            return 315000;
        } else {
            return 350000;
        }
    }
}
