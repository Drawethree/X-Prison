package me.drawethree.ultraprisoncore.autominer;

import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class AutoMinerRegion {

	private UltraPrisonAutoMiner parent;
	private World world;
	private IWrappedRegion region;
	private List<String> commands;

	private Task autoMinerTask;


	public AutoMinerRegion(UltraPrisonAutoMiner parent, World world, IWrappedRegion region, List<String> rewards, int seconds) {
		this.parent = parent;
		this.world = world;
		this.region = region;
		this.commands = rewards;

		this.autoMinerTask = Schedulers.async().runRepeating(() -> {
			for (Player p : Players.all()) {

				if (!p.getWorld().equals(this.world)) {
					continue;
				}

				if (region.contains(p.getLocation())) {
					if (!parent.hasAutoMinerTime(p)) {
						parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_disabled"));
					} else {
						parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_enabled"));
						this.executeCommands(p);
						this.parent.decrementTime(p);
					}
				}
			}
		}, seconds, TimeUnit.SECONDS, seconds, TimeUnit.SECONDS);
	}

	private void executeCommands(Player p) {
		if (this.commands.isEmpty()) {
			return;
		}
		this.commands.forEach(c -> Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()))));
	}

}
