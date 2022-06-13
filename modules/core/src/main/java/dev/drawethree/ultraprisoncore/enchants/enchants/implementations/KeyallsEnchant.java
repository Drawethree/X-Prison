package dev.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class KeyallsEnchant extends UltraPrisonEnchantment {

	private double chance;
	private List<String> commandsToExecute;

	public KeyallsEnchant(UltraPrisonEnchants instance) {
		super(instance, 18);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (chance * enchantLevel > ThreadLocalRandom.current().nextDouble(100)) {
			String randomCmd = this.commandsToExecute.get(ThreadLocalRandom.current().nextInt(commandsToExecute.size()));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomCmd.replace("%player%", e.getPlayer().getName()));
		}
	}

	@Override
	public void reload() {
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
		this.commandsToExecute = plugin.getConfig().get().getStringList("enchants." + id + ".Commands");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
