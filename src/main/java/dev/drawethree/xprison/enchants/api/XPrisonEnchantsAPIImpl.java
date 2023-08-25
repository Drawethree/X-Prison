package dev.drawethree.xprison.enchants.api;

import dev.drawethree.xprison.enchants.managers.EnchantsManager;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.repo.EnchantsRepository;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class XPrisonEnchantsAPIImpl implements XPrisonEnchantsAPI {

	private final EnchantsManager enchantsManager;
	private final EnchantsRepository enchantsRepository;

	public XPrisonEnchantsAPIImpl(EnchantsManager enchantsManager, EnchantsRepository enchantsRepository) {
		this.enchantsManager = enchantsManager;
		this.enchantsRepository = enchantsRepository;
	}

	@Override
	public Map<XPrisonEnchantment, Integer> getEnchants(ItemStack pickAxe) {
		return this.enchantsManager.getItemEnchants(pickAxe);
	}

	@Override
	public boolean hasEnchant(ItemStack item, XPrisonEnchantment enchant) {
		return getEnchantLevel(item, enchant) != 0;
	}

	@Override
	public int getEnchantLevel(ItemStack item, XPrisonEnchantment enchantment) {
		return this.enchantsManager.getEnchantLevel(item, enchantment);
	}

	@Override
	public ItemStack setEnchantLevel(Player player, ItemStack item, XPrisonEnchantment enchantment, int level) {
		return this.enchantsManager.setEnchantLevel(player, item, enchantment, level);
	}

	@Override
	public ItemStack removeEnchant(Player player, ItemStack item, XPrisonEnchantment enchantment) {
		return this.enchantsManager.removeEnchant(player, item, enchantment);
	}

	@Override
	public XPrisonEnchantment getById(int id) {
		return this.enchantsRepository.getEnchantById(id);
	}

	@Override
	public XPrisonEnchantment getByName(String rawName) {
		return this.enchantsRepository.getEnchantByName(rawName);
	}

	@Override
	public boolean registerEnchant(XPrisonEnchantment enchantment) {
		return this.enchantsRepository.register(enchantment);
	}

	@Override
	public boolean unregisterEnchant(XPrisonEnchantment enchantment) {
		return this.enchantsRepository.unregister(enchantment);
	}
}
