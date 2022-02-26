package dev.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CharityEnchant extends UltraPrisonEnchantment {

	private double chance;
	private long minAmount;
	private long maxAmount;

	public CharityEnchant(UltraPrisonEnchants instance) {
		super(instance, 11);
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
		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			long randAmount;

			for (Player p : Players.all()) {
				randAmount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
				plugin.getCore().getEconomy().depositPlayer(p, randAmount);

				if (!this.isMessagesEnabled()) {
					continue;
				}

				if (p.equals(e.getPlayer())) {
					PlayerUtils.sendMessage(p, plugin.getMessage("charity_your").replace("%amount%", String.format("%,d", randAmount)));
				} else {
					PlayerUtils.sendMessage(p, plugin.getMessage("charity_other").replace("%amount%", String.format("%,d", randAmount)).replace("%player%", e.getPlayer().getName()));
				}
			}
		}
	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.minAmount = plugin.getConfig().get().getLong("enchants." + id + ".Min-Money");
		this.maxAmount = plugin.getConfig().get().getLong("enchants." + id + ".Max-Money");
	}
}
