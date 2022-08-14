package dev.drawethree.ultraprisoncore.enchants.api;

import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class UltraPrisonEnchantsAPIImpl implements UltraPrisonEnchantsAPI {

	private final EnchantsManager enchantsManager;

	public UltraPrisonEnchantsAPIImpl(EnchantsManager enchantsManager) {

		this.enchantsManager = enchantsManager;
	}

	@Override
	public Map<UltraPrisonEnchantment, Integer> getEnchants(ItemStack pickAxe) {
		return this.enchantsManager.getItemEnchants(pickAxe);
	}

	@Override
	public boolean hasEnchant(ItemStack item, UltraPrisonEnchantment enchant) {
		return getEnchantLevel(item, enchant) != 0;
	}

	@Override
	public int getEnchantLevel(ItemStack item, UltraPrisonEnchantment enchantment) {
		return this.enchantsManager.getEnchantLevel(item, enchantment);
	}

	@Override
	public ItemStack setEnchantLevel(Player player, ItemStack item, UltraPrisonEnchantment enchantment, int level) {
		return this.enchantsManager.setEnchantLevel(player, item, enchantment, level);
	}

	@Override
	public ItemStack removeEnchant(Player player, ItemStack item, UltraPrisonEnchantment enchantment) {
		return this.enchantsManager.removeEnchant(player, item, enchantment);
	}

	@Override
	public UltraPrisonEnchantment getById(int id) {
		return UltraPrisonEnchantment.getEnchantById(id);
	}

	@Override
	public UltraPrisonEnchantment getByName(String rawName) {
		return UltraPrisonEnchantment.getEnchantByName(rawName);
	}
}
