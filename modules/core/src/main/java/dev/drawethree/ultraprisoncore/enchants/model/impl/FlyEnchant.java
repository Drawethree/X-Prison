package dev.drawethree.ultraprisoncore.enchants.model.impl;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.model.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public final class FlyEnchant extends UltraPrisonEnchantment {



	public FlyEnchant(UltraPrisonEnchants instance) {
		super(instance, 8);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		p.setAllowFlight(true);

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		p.setAllowFlight(false);
	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
	}

	@Override
	public double getChanceToTrigger(int enchantLevel) {
		return 100.0;
	}

	@Override
	public void reload() {
		super.reload();
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
