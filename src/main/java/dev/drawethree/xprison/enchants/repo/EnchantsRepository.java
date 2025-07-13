package dev.drawethree.xprison.enchants.repo;


import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.warning;

public class EnchantsRepository {

	private final XPrisonEnchants plugin;

	private final Map<Integer, XPrisonEnchantment> enchantsById;
	private final Map<String, XPrisonEnchantment> enchantsByName;

	public EnchantsRepository(XPrisonEnchants plugin) {
		this.plugin = plugin;
		this.enchantsById = new HashMap<>();
		this.enchantsByName = new HashMap<>();
	}

	public Collection<XPrisonEnchantment> getAll() {
		return enchantsById.values();
	}

	public XPrisonEnchantment getEnchantBy(Object object) {
		if (object instanceof Integer) {
			return getEnchantById((int) object);
		} else if (object instanceof String) {
			final String s = String.valueOf(object);
			try {
				return getEnchantById(Integer.parseInt(s));
			} catch (NumberFormatException e) {
				return getEnchantByName(s);
			}
		} else {
			throw new IllegalArgumentException("Illegal argument. Cannot get enchant by: " + object);
		}
	}

	public XPrisonEnchantment getEnchantById(int id) {
		return enchantsById.get(id);
	}

	public XPrisonEnchantment getEnchantByName(String name) {
		return enchantsByName.get(name.toLowerCase());
	}

	public void reload() {

		enchantsById.values().forEach(XPrisonEnchantment::load);

		info(TextUtils.applyColor("&aReloaded all enchants."));
	}

	public boolean register(XPrisonEnchantment enchantment) {

		if (enchantsById.containsKey(enchantment.getId()) || enchantsByName.containsKey(enchantment.getRawName())) {
			warning(TextUtils.applyColor("Unable to register enchant " + enchantment.getName() + " created by " + enchantment.getAuthor() + ". That enchant is already registered."));
			return false;
		}

		Validate.notNull(enchantment.getRawName());

		enchantsById.put(enchantment.getId(), enchantment);
		enchantsByName.put(enchantment.getRawName().toLowerCase(), enchantment);

		info(TextUtils.applyColor("&aRegistered enchant " + enchantment.getName() + "&a created by &e" + enchantment.getAuthor()));
		return true;
	}

	public boolean unregister(XPrisonEnchantment enchantment) {

		if (!enchantsById.containsKey(enchantment.getId()) && !enchantsByName.containsKey(enchantment.getRawName())) {
			warning(TextUtils.applyColor("Unable to unregister enchant " + enchantment.getName() + " created by " + enchantment.getAuthor() + ". That enchant is not registered."));
			return false;
		}

		enchantsById.remove(enchantment.getId());
		enchantsByName.remove(enchantment.getRawName());

		info(TextUtils.applyColor("&aUnregistered enchant " + enchantment.getName() + "&a created by &e" + enchantment.getAuthor()));
		return true;
	}
}
