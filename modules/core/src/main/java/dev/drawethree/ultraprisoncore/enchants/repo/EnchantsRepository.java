package dev.drawethree.ultraprisoncore.enchants.repo;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.model.impl.*;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnchantsRepository {

	private final UltraPrisonEnchants plugin;

	private final Map<Integer, UltraPrisonEnchantment> enchantsById;
	private final Map<String, UltraPrisonEnchantment> enchantsByName;

	public EnchantsRepository(UltraPrisonEnchants plugin) {
		this.plugin = plugin;
		this.enchantsById = new HashMap<>();
		this.enchantsByName = new HashMap<>();
	}

	public Collection<UltraPrisonEnchantment> getAll() {
		return enchantsById.values();
	}

	public UltraPrisonEnchantment getEnchantById(int id) {
		return enchantsById.get(id);
	}

	public UltraPrisonEnchantment getEnchantByName(String name) {
		return enchantsByName.get(name.toLowerCase());
	}

	public void reload() {

		enchantsById.values().forEach(UltraPrisonEnchantment::reload);

		UltraPrisonCore.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully reloaded all enchants."));
	}

	public void loadDefaultEnchantments() {
		register(new EfficiencyEnchant(this.plugin));
		register(new UnbreakingEnchant(this.plugin));
		register(new FortuneEnchant(this.plugin));
		register(new HasteEnchant(this.plugin));
		register(new SpeedEnchant(this.plugin));
		register(new JumpBoostEnchant(this.plugin));
		register(new NightVisionEnchant(this.plugin));
		register(new FlyEnchant(this.plugin));
		register(new ExplosiveEnchant(this.plugin));
		register(new LayerEnchant(this.plugin));
		register(new CharityEnchant(this.plugin));
		register(new SalaryEnchant(this.plugin));
		register(new BlessingEnchant(this.plugin));
		register(new TokenatorEnchant(this.plugin));
		register(new KeyFinderEnchant(this.plugin));
		register(new PrestigeFinderEnchant(this.plugin));
		register(new BlockBoosterEnchant(this.plugin));
		register(new KeyallsEnchant(this.plugin));
		if (UltraPrisonCore.getInstance().isUltraBackpacksEnabled()) {
			register(new BackpackAutoSellEnchant(this.plugin));
		} else {
			register(new AutoSellEnchant(this.plugin));
		}
		register(new VoucherFinderEnchant(this.plugin));
		register(new NukeEnchant(this.plugin));
		register(new GemFinderEnchant(this.plugin));
		register(new GangValueFinderEnchant(this.plugin));
	}

	public boolean register(UltraPrisonEnchantment enchantment) {

		if (enchantsById.containsKey(enchantment.getId()) || enchantsByName.containsKey(enchantment.getRawName())) {
			UltraPrisonCore.getInstance().getLogger().warning(TextUtils.applyColor("&cUnable to register enchant " + enchantment.getName() + "&c created by " + enchantment.getAuthor() + ". That enchant is already registered."));
			return false;
		}

		Validate.notNull(enchantment.getRawName());

		enchantsById.put(enchantment.getId(), enchantment);
		enchantsByName.put(enchantment.getRawName().toLowerCase(), enchantment);

		UltraPrisonCore.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully registered enchant " + enchantment.getName() + "&a created by " + enchantment.getAuthor()));
		return true;
	}

	public boolean unregister(UltraPrisonEnchantment enchantment) {

		if (!enchantsById.containsKey(enchantment.getId()) && !enchantsByName.containsKey(enchantment.getRawName())) {
			UltraPrisonCore.getInstance().getLogger().warning(TextUtils.applyColor("&cUnable to unregister enchant " + enchantment.getName() + "&c created by " + enchantment.getAuthor() + ". That enchant is not registered."));
			return false;
		}

		enchantsById.remove(enchantment.getId());
		enchantsByName.remove(enchantment.getRawName());

		UltraPrisonCore.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully unregistered enchant " + enchantment.getName() + "&a created by " + enchantment.getAuthor()));
		return true;
	}
}
