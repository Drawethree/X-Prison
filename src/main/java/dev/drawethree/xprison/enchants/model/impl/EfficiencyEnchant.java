package dev.drawethree.xprison.enchants.model.impl;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class EfficiencyEnchant extends XPrisonEnchantment {
	public EfficiencyEnchant(XPrisonEnchants instance) {
		super(instance, 1);
	}

	@Override
	public void onEquip(Player p, @NotNull ItemStack pickAxe, int level) {
		plugin.getCore().debug("Adding enchantment " + this.getName() + " to pickaxe " + pickAxe.getType().name() + " with level " + level, plugin);
		ItemMeta meta = pickAxe.getItemMeta();
		meta.addEnchant(Enchantment.EFFICIENCY, level, true);
		pickAxe.setItemMeta(meta);
	}

	@Override
	public void onUnequip(Player p, @NotNull ItemStack pickAxe, int level) {
		plugin.getCore().debug("Removing enchantment " + this.getName() + " from pickaxe " + pickAxe.getType().name() + " with level " + level, plugin);
        ItemMeta meta = pickAxe.getItemMeta();
        if (level == 0){
            if (meta.hasEnchant(Enchantment.EFFICIENCY)) {
				meta.removeEnchant(Enchantment.EFFICIENCY);
			}
			pickAxe.setItemMeta(meta);
		} else {
            meta.addEnchant(Enchantment.EFFICIENCY, level, false);
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
