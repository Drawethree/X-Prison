package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.RegionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class AutoSellEnchant extends UltraPrisonEnchantment {

	private double chance;

	public AutoSellEnchant(UltraPrisonEnchants instance) {
		super(instance, 19);
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
			this.plugin.getCore().debug("AutoSellEnchant::isMessagesEnabled >> " + this.isMessagesEnabled());
			this.plugin.getCore().getAutoSell().sellAll(e.getPlayer(), RegionUtils.getRegionWithHighestPriority(e.getPlayer().getLocation()), this.isMessagesEnabled());
		}

	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
	}
}
