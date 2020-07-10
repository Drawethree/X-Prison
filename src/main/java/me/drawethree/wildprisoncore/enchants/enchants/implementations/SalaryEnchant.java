package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
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
        this.minAmount = instance.getConfig().get().getInt("enchants." + id + ".Min-Money");
        this.maxAmount = instance.getConfig().get().getInt("enchants." + id + ".Max-Money");
        this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
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

            plugin.getCore().getEconomy().depositPlayer(e.getPlayer(), randAmount);
            e.getPlayer().sendMessage("Debug Message: Salary enchant triggered, giving you " + String.format("$%,.0f", randAmount));
        }

    }
}
