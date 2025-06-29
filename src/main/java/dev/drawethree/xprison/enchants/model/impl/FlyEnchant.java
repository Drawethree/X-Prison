package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.api.enchants.model.EquipabbleEnchantment;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class FlyEnchant extends XPrisonEnchantmentBaseCore implements EquipabbleEnchantment {

	public FlyEnchant() {

	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		p.setAllowFlight(true);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		p.setAllowFlight(false);
	}
}
