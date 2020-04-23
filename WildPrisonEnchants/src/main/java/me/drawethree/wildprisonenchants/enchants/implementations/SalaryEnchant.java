package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SalaryEnchant extends WildPrisonEnchantment {

    private double minAmount;
    private double maxAmount;
    private double chance;

    public SalaryEnchant(WildPrisonEnchants instance) {
        super(instance, 12);
        this.minAmount = instance.getConfig().getInt("enchants." + id + ".Min-Money");
        this.maxAmount = instance.getConfig().getInt("enchants." + id + ".Max-Money");
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

        if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {

            double randAmount = ThreadLocalRandom.current().nextDouble(minAmount, maxAmount);

            plugin.getEconomy().depositPlayer(e.getPlayer(), randAmount);
        }

    }
}
