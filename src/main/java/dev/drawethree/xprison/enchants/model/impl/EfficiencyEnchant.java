package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XEnchantment;

import dev.drawethree.xprison.api.enchants.model.EquipabbleEnchantment;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EfficiencyEnchant extends XPrisonEnchantmentBaseCore implements EquipabbleEnchantment {

	public EfficiencyEnchant() {
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		ItemMeta meta = pickAxe.getItemMeta();
		meta.addEnchant(XEnchantment.EFFICIENCY.get(), level, true);
		pickAxe.setItemMeta(meta);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}
}
