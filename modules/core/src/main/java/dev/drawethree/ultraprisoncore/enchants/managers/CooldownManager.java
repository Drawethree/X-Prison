package dev.drawethree.ultraprisoncore.enchants.managers;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CooldownManager {

	private final UltraPrisonEnchants plugin;
	private final CooldownMap<Player> valueCooldown;

	public CooldownManager(UltraPrisonEnchants plugin) {
		this.plugin = plugin;
		this.valueCooldown = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));
	}

	public boolean hasValueCooldown(Player sender) {
		return !valueCooldown.test(sender);
	}

	public long getRemainingTime(Player sender) {
		return valueCooldown.get(sender).remainingTime(TimeUnit.SECONDS);
	}
}
