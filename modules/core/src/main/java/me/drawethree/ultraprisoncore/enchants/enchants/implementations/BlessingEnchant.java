package me.drawethree.ultraprisoncore.enchants.enchants.implementations;

import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class BlessingEnchant extends UltraPrisonEnchantment {

	private double chance;
	private long minAmount;
	private long maxAmount;

	public BlessingEnchant(UltraPrisonEnchants instance) {
		super(instance, 13);
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
				plugin.getCore().getTokens().getTokensManager().giveTokens(p, randAmount, null, ReceiveCause.MINING_OTHERS);

				if (!this.isMessagesEnabled()) {
					continue;
				}

				if (p.equals(e.getPlayer())) {
					PlayerUtils.sendMessage(p, plugin.getMessage("blessing_your").replace("%amount%", String.format("%,d", randAmount)));
				} else {
					PlayerUtils.sendMessage(p, plugin.getMessage("blessing_other").replace("%amount%", String.format("%,d", randAmount)).replace("%player%", e.getPlayer().getName()));
				}
			}
		}
	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.minAmount = plugin.getConfig().get().getLong("enchants." + id + ".Min-Tokens");
		this.maxAmount = plugin.getConfig().get().getLong("enchants." + id + ".Max-Tokens");
	}
}
