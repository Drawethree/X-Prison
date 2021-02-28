package me.drawethree.ultraprisoncore.pickaxelevels.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor

public class PickaxeLevel {

	@Getter
	private int level;
	@Getter
	private long blocksRequired;
	@Getter
	private String displayName;

	private List<String> rewards;

	public void giveRewards(Player p) {
		if (!Bukkit.isPrimaryThread()) {
			Schedulers.sync().run(() -> this.rewards.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()))));
		} else {
			this.rewards.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName())));
		}
	}

	public String getDisplayName(Player p) {
		return this.displayName.replace("%player%", p.getName());
	}
}
