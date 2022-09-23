package dev.drawethree.ultraprisoncore.enchants.api;

import dev.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.repo.EnchantsRepository;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class UltraPrisonEnchantsAPIImpl implements UltraPrisonEnchantsAPI {

	private final EnchantsManager enchantsManager;
	private final EnchantsRepository enchantsRepository;

	public UltraPrisonEnchantsAPIImpl(EnchantsManager enchantsManager, EnchantsRepository enchantsRepository) {
		this.enchantsManager = enchantsManager;
		this.enchantsRepository = enchantsRepository;
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
		return this.enchantsRepository.getEnchantById(id);
	}

	@Override
	public UltraPrisonEnchantment getByName(String rawName) {
		return this.enchantsRepository.getEnchantByName(rawName);
	}

	@Override
	public boolean registerEnchant(UltraPrisonEnchantment enchantment) {
		return this.enchantsRepository.register(enchantment);
	}

	@Override
	public boolean unregisterEnchant(UltraPrisonEnchantment enchantment) {
		return this.enchantsRepository.unregister(enchantment);
	}
}
