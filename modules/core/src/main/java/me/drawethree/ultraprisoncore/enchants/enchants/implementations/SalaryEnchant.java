package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SalaryEnchant extends UltraPrisonEnchantment {

	private long minAmount;
	private long maxAmount;
	private double chance;

	public SalaryEnchant(UltraPrisonEnchants instance) {
		super(instance, 12);
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
			double randAmount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextLong(minAmount, maxAmount);

			boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

			plugin.getCore().getEconomy().depositPlayer(e.getPlayer(), luckyBooster ? randAmount * 2 : randAmount);
			if (this.plugin.isAutoSellModule()) {
				plugin.getCore().getAutoSell().addToCurrentEarnings(e.getPlayer(), luckyBooster ? randAmount * 2 : randAmount);
			}
		}

	}

	@Override
	public void reload() {
		this.minAmount = plugin.getConfig().get().getLong("enchants." + id + ".Min-Money");
		this.maxAmount = plugin.getConfig().get().getLong("enchants." + id + ".Max-Money");
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
