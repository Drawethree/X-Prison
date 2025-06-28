package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XEnchantment;

import dev.drawethree.xprison.api.enchants.model.EquipabbleEnchantment;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentAbstract;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EfficiencyEnchant extends XPrisonEnchantmentAbstract implements EquipabbleEnchantment {
	public EfficiencyEnchant(XPrisonEnchants instance) {
		super(instance, 1);
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

	@Override
	public double getChanceToTrigger(int enchantLevel) {
		return 100.0;
	}

	@Override
	public void reload() {
		super.reload();
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
