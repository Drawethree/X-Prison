package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SalaryEnchant extends WildPrisonEnchantment {

    private long minAmount;
    private long maxAmount;
    private double chance;

    public SalaryEnchant(WildPrisonEnchants instance) {
        super(instance, 12);
        this.minAmount = instance.getConfig().get().getLong("enchants." + id + ".Min-Money");
        this.maxAmount = instance.getConfig().get().getLong("enchants." + id + ".Max-Money");
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

        if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
            double randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);

			boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

			plugin.getCore().getEconomy().depositPlayer(e.getPlayer(), luckyBooster ? randAmount * 2 : randAmount);
			plugin.getCore().getAutoSell().addToCurrentEarnings(e.getPlayer(), luckyBooster ? randAmount * 2 : randAmount);
        }

    }
}
