package dev.drawethree.xprison.enchants.managers;

import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CooldownManager {

	private final CooldownMap<Player> valueCooldown;

	public CooldownManager() {
		this.valueCooldown = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));
	}

	public boolean hasValueCooldown(Player sender) {
		return !valueCooldown.test(sender);
	}

	public long getRemainingTime(Player sender) {
		return valueCooldown.get(sender).remainingTime(TimeUnit.SECONDS);
	}
}
