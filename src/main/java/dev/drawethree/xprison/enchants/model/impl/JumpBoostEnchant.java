package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class JumpBoostEnchant extends XPrisonEnchantment {
	public JumpBoostEnchant(XPrisonEnchants instance) {
		super(instance, 6);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		if (level == 0) {
			this.onUnequip(p, pickAxe, level);
			return;
		}
		PotionEffect effect = new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, level - 1, true, true);

		if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
			effect.apply(p);
		} else {
			p.addPotionEffect(new PotionEffect(PotionEffectType.getById(8), Integer.MAX_VALUE, level - 1, true, true), true);
		}
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		if (MinecraftVersion.olderThan(MinecraftVersion.V.v1_13)) {
			p.removePotionEffect(PotionEffectType.JUMP_BOOST);
		} else {
			p.removePotionEffect(PotionEffectType.getById(8));
		}
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
