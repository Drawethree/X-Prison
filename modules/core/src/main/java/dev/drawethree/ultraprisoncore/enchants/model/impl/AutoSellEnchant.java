package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class AutoSellEnchant extends UltraPrisonEnchantment {

	private double chance;

	public AutoSellEnchant(UltraPrisonEnchants instance) {
		super(instance, 19);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
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

		if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			if (this.plugin.getCore().isModuleEnabled(UltraPrisonAutoSell.MODULE_NAME)) {
				this.plugin.getCore().getAutoSell().getManager().sellAll(e.getPlayer(), RegionUtils.getRegionWithHighestPriority(e.getPlayer().getLocation()));
			}
		}

	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
	}
}
