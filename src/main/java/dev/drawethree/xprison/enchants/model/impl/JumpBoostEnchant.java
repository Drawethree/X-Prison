package dev.drawethree.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XPotion;
import dev.drawethree.xprison.enchants.model.EquipabbleEnchantment;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantmentBaseCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public final class JumpBoostEnchant extends XPrisonEnchantmentBaseCore implements EquipabbleEnchantment {

	public JumpBoostEnchant() {

	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		if (level == 0) {
			this.onUnequip(p, pickAxe, level);
			return;
		}
		p.addPotionEffect(new PotionEffect(XPotion.JUMP_BOOST.get(), Integer.MAX_VALUE, level - 1, true, true), true);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		p.removePotionEffect(XPotion.JUMP_BOOST.get());
	}
}
