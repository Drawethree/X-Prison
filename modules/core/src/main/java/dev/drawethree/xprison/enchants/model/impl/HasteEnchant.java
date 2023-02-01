package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class HasteEnchant extends XPrisonEnchantment {

	public HasteEnchant(XPrisonEnchants instance) {
		super(instance, 4);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		if (level == 0) {
			this.onUnequip(p, pickAxe, level);
			return;
		}
		p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, level - 1, true, true), true);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		p.removePotionEffect(PotionEffectType.FAST_DIGGING);
	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

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
