package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class TokenatorEnchant extends WildPrisonEnchantment {
    private final long maxAmount;
    private final long minAmount;
    private final double chance;


    public TokenatorEnchant(WildPrisonEnchants instance) {
        super(instance, 14);
        this.minAmount = instance.getConfig().getLong("enchants." + id + ".Min-Tokens");
        this.maxAmount = instance.getConfig().getLong("enchants." + id + ".Max-Tokens");
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {
            long randAmount;
            randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
            WildPrisonTokens.getApi().addTokens(e.getPlayer(), randAmount);
        }
    }
}
