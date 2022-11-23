package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class PrestigeFinderEnchant extends UltraPrisonEnchantment {

	private double chance;
	private String commandToExecute;
	private String amountToGiveExpression;

	public PrestigeFinderEnchant(UltraPrisonEnchants instance) {
		super(instance, 16);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.commandToExecute = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Command");
		this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			int levels = (int) createExpression(enchantLevel).evaluate();
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute.replace("%player%", e.getPlayer().getName()).replace("%amount%", String.valueOf(levels)));
		}
	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.commandToExecute = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Command");
		this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
	}

	private Expression createExpression(int level) {
		return new ExpressionBuilder(this.amountToGiveExpression)
				.variables("level")
				.build()
				.setVariable("level", level);
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
