package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultrabackpacks.api.UltraBackpacksAPI;
import dev.drawethree.ultrabackpacks.api.exception.BackpackNotFoundException;
import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class BackpackAutoSellEnchant extends UltraPrisonEnchantment {

	private double chance;

	public BackpackAutoSellEnchant(UltraPrisonEnchants instance) {
		super(instance, 19);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

		if (this.chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			if (UltraPrisonCore.getInstance().isUltraBackpacksEnabled()) {
				try {
					UltraBackpacksAPI.sellBackpack(e.getPlayer(), true);
				} catch (BackpackNotFoundException ignored) {
					this.plugin.getCore().debug("AutoSellEnchant::onBlockBreak > Player " + e.getPlayer().getName() + " does not have backpack.", this.plugin);
				}
			}
		}

	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
	}
}
