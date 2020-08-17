package me.drawethree.wildprisoncore.enchants.enchants.implementations;

import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.enchants.enchants.WildPrisonEnchantment;
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

public class LuckyBoosterEnchant extends WildPrisonEnchantment {

	private static final Map<UUID, Long> boostedPlayers = new HashMap<>();


	private final double chance;

	public LuckyBoosterEnchant(WildPrisonEnchants instance) {
		super(instance, 8);
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
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

			if (boostedPlayers.containsKey(e.getPlayer().getUniqueId())) {
				return;
			}

			e.getPlayer().sendMessage(this.plugin.getMessage("lucky_booster_on"));

			boostedPlayers.put(e.getPlayer().getUniqueId(), Time.nowMillis() + TimeUnit.MINUTES.toMillis(5));
			Schedulers.async().runLater(() -> {
				if (e.getPlayer().isOnline()) {
					e.getPlayer().sendMessage(this.plugin.getMessage("lucky_booster_off"));
				}
				boostedPlayers.remove(e.getPlayer().getUniqueId());
			}, 5, TimeUnit.MINUTES);
		}
	}

	public static boolean hasLuckyBoosterRunning(Player p) {
		return boostedPlayers.containsKey(p.getUniqueId());
	}

	public static String getTimeLeft(Player p) {

		if (!boostedPlayers.containsKey(p.getUniqueId())) {
			return "";
		}

		long endTime = boostedPlayers.get(p.getUniqueId());

		if (System.currentTimeMillis() > endTime) {
			return "";
		}


		long timeLeft = endTime - System.currentTimeMillis();

		long days = timeLeft / (24 * 60 * 60 * 1000);
		timeLeft -= days * (24 * 60 * 60 * 1000);

		long hours = timeLeft / (60 * 60 * 1000);
		timeLeft -= hours * (60 * 60 * 1000);

		long minutes = timeLeft / (60 * 1000);
		timeLeft -= minutes * (60 * 1000);

		long seconds = timeLeft / (1000);

		timeLeft -= seconds * 1000;

		return new StringBuilder().append(ChatColor.GRAY + "(" + ChatColor.WHITE).append(days).append("d ").append(hours).append("h ").append(minutes).append("m ").append(seconds).append("s").append(ChatColor.GRAY + ")").toString();
	}
}
