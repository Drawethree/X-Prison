package dev.drawethree.ultraprisoncore.enchants.api;

import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class UltraPrisonEnchantsAPIImpl implements UltraPrisonEnchantsAPI {

	private final EnchantsManager enchantsManager;

	public UltraPrisonEnchantsAPIImpl(EnchantsManager enchantsManager) {

		this.enchantsManager = enchantsManager;
	}

	@Override
	public Map<UltraPrisonEnchantment, Integer> getPlayerEnchants(ItemStack pickAxe) {
		return this.enchantsManager.getItemEnchants(pickAxe);
	}

	@Override
	public boolean hasEnchant(Player p, int id) {
		return this.enchantsManager.hasEnchant(p, id);
	}

	@Override
	public int getEnchantLevel(ItemStack item, int id) {
		return this.enchantsManager.getEnchantLevel(item, id);
	}

	@Override
	public ItemStack setEnchantLevel(Player player, ItemStack item, int id, int level) {
		return this.enchantsManager.setEnchantLevel(player, item, id, level);
	}

	@Override
	public ItemStack removeEnchant(Player player, ItemStack item, int id) {
		return this.enchantsManager.removeEnchant(player, item, id);
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
