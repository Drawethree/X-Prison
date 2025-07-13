package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XPotion;
import dev.drawethree.xprison.enchants.model.EquipabbleEnchantment;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public final class HasteEnchant extends XPrisonEnchantmentBaseCore implements EquipabbleEnchantment {

	public HasteEnchant() {
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		if (level == 0) {
			this.onUnequip(p, pickAxe, level);
			return;
		}
		p.addPotionEffect(new PotionEffect(XPotion.HASTE.get(), Integer.MAX_VALUE, level - 1, true, true), true);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		p.removePotionEffect(XPotion.HASTE.get());
	}
}
