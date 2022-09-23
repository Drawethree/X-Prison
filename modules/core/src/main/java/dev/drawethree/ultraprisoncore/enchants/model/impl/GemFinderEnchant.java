package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class GemFinderEnchant extends UltraPrisonEnchantment {

	private long maxAmount;
	private long minAmount;
	private double chance;

	public GemFinderEnchant(UltraPrisonEnchants instance) {
		super(instance, 22);
		this.minAmount = plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Min-Gems");
		this.maxAmount = plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Max-Gems");
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
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
			if (!this.plugin.getCore().isModuleEnabled(UltraPrisonGems.MODULE_NAME)) {
				return;
			}
			long randAmount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
			plugin.getCore().getGems().getGemsManager().giveGems(e.getPlayer(), randAmount, null, ReceiveCause.MINING);
		}
	}

	@Override
	public void reload() {
		super.reload();
		this.minAmount = plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Min-Gems");
		this.maxAmount = plugin.getEnchantsConfig().getYamlConfig().getLong("enchants." + id + ".Max-Gems");
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
