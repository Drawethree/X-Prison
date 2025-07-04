package dev.drawethree.xprison.bombs.service;

import dev.drawethree.xprison.bombs.XPrisonBombs;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public final class BombCooldownService {

	private final XPrisonBombs plugin;
	private CooldownMap<Player> cooldownMap;

	public BombCooldownService(XPrisonBombs plugin) {
		this.cooldownMap = CooldownMap.create(Cooldown.of(plugin.getConfig().getBombThrowCooldown(), TimeUnit.SECONDS));
		this.plugin = plugin;
	}

	public boolean isInCooldown(Player player) {
		return !this.cooldownMap.test(player);
	}

	public int getRemainingCooldown(Player player) {
		return (int) cooldownMap.remainingTime(player, TimeUnit.SECONDS);
	}

	public void reload() {
		this.cooldownMap = CooldownMap.create(Cooldown.of(plugin.getConfig().getBombThrowCooldown(), TimeUnit.SECONDS));
	}
}
