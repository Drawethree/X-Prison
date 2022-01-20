package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class GemFinderEnchant extends UltraPrisonEnchantment {

	private long maxAmount;
	private long minAmount;
	private double chance;


	public GemFinderEnchant(UltraPrisonEnchants instance) {
		super(instance, 22);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			long randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
			plugin.getCore().getGems().getGemsManager().giveGems(e.getPlayer(), randAmount, null, ReceiveCause.MINING);
		}
	}

	@Override
	public void reload() {
		this.minAmount = plugin.getConfig().get().getLong("enchants." + id + ".Min-Gems");
		this.maxAmount = plugin.getConfig().get().getLong("enchants." + id + ".Max-Gems");
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
