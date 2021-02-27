package me.drawethree.ultraprisoncore.ranks.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
@Getter
public class Prestige {

	private long id;
	private double cost;
	private String prefix;
	private List<String> commandsToExecute;

	public void runCommands(Player p) {
		if (commandsToExecute != null) {

			if (!Bukkit.isPrimaryThread()) {
				Schedulers.sync().run(() -> {
					executeCommands(p);
				});
			} else {
				executeCommands(p);
			}
		}
	}

	private void executeCommands(Player p) {
		for (String cmd : commandsToExecute) {
			if (!Bukkit.isPrimaryThread()) {
				Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Prestige%", prefix)));
			} else {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Prestige%", prefix));
			}
		}
	}

}
