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

public final class LuckyBoosterEnchant extends UltraPrisonEnchantment {

	private static final Map<UUID, Long> BOOSTED_PLAYERS = new HashMap<>();

	private double chance;

	public LuckyBoosterEnchant(UltraPrisonEnchants instance) {
		super(instance, 8);
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
	}

	public static boolean hasLuckyBoosterRunning(Player p) {
		return BOOSTED_PLAYERS.containsKey(p.getUniqueId());
	}

	public static String getTimeLeft(Player p) {

		if (!BOOSTED_PLAYERS.containsKey(p.getUniqueId())) {
			return "";
		}

		long endTime = BOOSTED_PLAYERS.get(p.getUniqueId());

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

		return ChatColor.GRAY + "(" + ChatColor.WHITE + days + "d " + hours + "h " + minutes + "m " + seconds + "s" + ChatColor.GRAY + ")";
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

			if (BOOSTED_PLAYERS.containsKey(e.getPlayer().getUniqueId())) {
				return;
			}

			PlayerUtils.sendMessage(e.getPlayer(), this.plugin.getEnchantsConfig().getMessage("lucky_booster_on"));

			BOOSTED_PLAYERS.put(e.getPlayer().getUniqueId(), Time.nowMillis() + TimeUnit.MINUTES.toMillis(5));
			Schedulers.sync().runLater(() -> {
				if (e.getPlayer().isOnline()) {
					PlayerUtils.sendMessage(e.getPlayer(), this.plugin.getEnchantsConfig().getMessage("lucky_booster_off"));
				}
				BOOSTED_PLAYERS.remove(e.getPlayer().getUniqueId());
			}, 5, TimeUnit.MINUTES);
		}
	}

	@Override
	public void reload() {
		super.reload();
		this.chance = plugin.getEnchantsConfig().getYamlConfig().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
