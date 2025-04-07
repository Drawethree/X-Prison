package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class FlyEnchant extends XPrisonEnchantment {



	public FlyEnchant(XPrisonEnchants instance) {
		super(instance, 8);
	}

	@Override
	public void onEquip(@NotNull Player p, ItemStack pickAxe, int level) {
		p.setAllowFlight(true);
	}

	@Override
	public void onUnequip(@NotNull Player p, ItemStack pickAxe, int level) {
		p.setAllowFlight(false);
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
