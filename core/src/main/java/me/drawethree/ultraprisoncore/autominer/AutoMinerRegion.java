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
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class AutoMinerRegion {

	private UltraPrisonAutoMiner parent;
	private World world;
	private IWrappedRegion region;
	private List<String> commands;
	private Task autoMinerTask;

	public AutoMinerRegion(UltraPrisonAutoMiner parent, World world, IWrappedRegion region, List<String> rewards, int rewardPeriod) {
		this.parent = parent;
		this.world = world;
		this.region = region;
		this.commands = rewards;

		AtomicInteger counter = new AtomicInteger();

		this.autoMinerTask = Schedulers.async().runRepeating(() -> {

			int current = counter.getAndIncrement();

			for (Player p : Players.all()) {

				if (!p.getWorld().equals(this.world)) {
					continue;
				}

				if (region.contains(p.getLocation())) {
					if (!parent.hasAutoMinerTime(p)) {
						parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_disabled"));
					} else {
						parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_enabled"));
						if (current >= rewardPeriod) {
							this.executeCommands(p);
						}
						this.parent.decrementTime(p);
					}
				}
			}

			if (current >= rewardPeriod) {
				counter.set(0);
			}
		}, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
	}

	private void executeCommands(Player p) {
		if (this.commands.isEmpty()) {
			return;
		}
		this.commands.forEach(c -> Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()))));
	}

}
