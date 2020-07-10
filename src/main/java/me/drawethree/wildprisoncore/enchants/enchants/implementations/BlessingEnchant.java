package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class BlessingEnchant extends WildPrisonEnchantment {
   /* private final double chance;
    private final long minAmount;
    private final long maxAmount;

    */

    public BlessingEnchant(WildPrisonEnchants instance) {
        super(instance, 13);
        /*this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
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

        double chance = enchantLevel < 25 ? 0.00002 : enchantLevel < 50 ? 0.0000167 : enchantLevel < 75 ? 0.0000143 : enchantLevel < 100 ? 0.0000125 : 0.00001;

        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {

            long selfAmount = enchantLevel < 25 ? 10000000 : enchantLevel < 50 ? 20000000 : enchantLevel < 75 ? 30000000 : enchantLevel < 100 ? 40000000 : 50000000;
            long othersAmount = selfAmount / 10;

            for (Player p : Players.all()) {
                if (p.equals(e.getPlayer())) {
                    plugin.getCore().getTokens().getTokensManager().giveTokens(p, selfAmount, null);
                    p.sendMessage(plugin.getMessage("blessing_your").replace("%amount%", String.format("%,d", selfAmount)));
                } else {
                    plugin.getCore().getTokens().getTokensManager().giveTokens(p, othersAmount, null);
                    p.sendMessage(plugin.getMessage("blessing_other").replace("%amount%", String.format("%,d", othersAmount)).replace("%player%", e.getPlayer().getName()));
                }
            }
        }
    }
}
