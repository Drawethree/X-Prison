package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.concurrent.ThreadLocalRandom;

public class SalaryEnchant extends WildPrisonEnchantment {

    private double minAmount;
    private double maxAmount;
    private double chance;

    public SalaryEnchant(WildPrisonEnchants instance) {
        super(instance,12);
        this.minAmount = instance.getConfig().getInt("enchants." + id + ".Min-money");
        this.maxAmount = instance.getConfig().getInt("enchants." + id + ".Max-money");
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

        if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {

            double randAmount = ThreadLocalRandom.current().nextDouble(minAmount, maxAmount);

            plugin.getEconomy().depositPlayer(e.getPlayer(), randAmount);
        }

    }
}
