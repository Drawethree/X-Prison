package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class CharityEnchant extends UltraPrisonEnchantment {

	private double chance;
	private String amountToGiveExpression;

	public CharityEnchant(UltraPrisonEnchants instance) {
		super(instance, 11);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		double chance = getChanceToTrigger(enchantLevel);
		if (chance >= ThreadLocalRandom.current().nextDouble(100)) {
			if (!this.plugin.getCore().isModuleEnabled(UltraPrisonTokens.MODULE_NAME)) {
				return;
			}
			long amount = (long) createExpression(enchantLevel).evaluate();

			for (Player p : Players.all()) {
				plugin.getCore().getEconomy().depositPlayer(p, amount);

				if (!this.isMessagesEnabled()) {
					continue;
				}

				if (p.equals(e.getPlayer())) {
					PlayerUtils.sendMessage(p, plugin.getEnchantsConfig().getMessage("charity_your").replace("%amount%", String.format("%,d", amount)));
				} else {
					PlayerUtils.sendMessage(p, plugin.getEnchantsConfig().getMessage("charity_other").replace("%amount%", String.format("%,d", amount)).replace("%player%", e.getPlayer().getName()));
				}
			}
		}
	}

	@Override
	public double getChanceToTrigger(int enchantLevel) {
		return chance * enchantLevel;
	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
		this.amountToGiveExpression = plugin.getEnchantsConfig().getYamlConfig().getString("enchants." + id + ".Amount-To-Give");
	}

	private Expression createExpression(int level) {
		return new ExpressionBuilder(this.amountToGiveExpression)
				.variables("level")
				.build()
				.setVariable("level", level);
	}
}
