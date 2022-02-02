package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FortuneEnchant extends UltraPrisonEnchantment {

	public FortuneEnchant(UltraPrisonEnchants instance) {
		super(instance, 3);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		ItemMeta meta = pickAxe.getItemMeta();
		meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, level, true);
		pickAxe.setItemMeta(meta);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

	}

	@Override
	public void reload() {

	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
