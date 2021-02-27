package me.drawethree.ultraprisoncore.ranks.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
@Getter
public class Rank {

	private int id;
	private double cost;
	private String prefix;
	private List<String> commandsToExecute;

	public void runCommands(Player p) {
		if (commandsToExecute != null) {

			if (!Bukkit.isPrimaryThread()) {
				Schedulers.async().run(() -> {
					for (String cmd : commandsToExecute) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Prestige%", prefix));
					}
				});
			} else {
				for (String cmd : commandsToExecute) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Prestige%", prefix));
				}
			}
		}
	}

}
