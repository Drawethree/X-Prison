package dev.drawethree.xprison.autominer.model;

import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import lombok.Getter;
import me.lucko.helper.utils.Players;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AutoMinerRegion {

	private final XPrisonAutoMiner plugin;
	private World world;
	private IWrappedRegion region;
	private List<String> commands;
	private int rewardPeriod;
	private int blocksBroken;
	private final AutoMinerTask autoMinerTask;

	public AutoMinerRegion(XPrisonAutoMiner plugin, World world, IWrappedRegion region, List<String> rewards, int rewardPeriod, int blocksBroken) {
		this.plugin = plugin;
		this.world = world;
		this.region = region;
		this.commands = rewards;
		this.rewardPeriod = rewardPeriod;
		this.blocksBroken = blocksBroken;
		this.autoMinerTask = new AutoMinerTask(this);
	}

	public void startAutoMinerTask() {
		this.autoMinerTask.start();
	}

	public List<Player> getPlayersInRegion() {
		List<Player> list = new ArrayList<>();
		for (Player p : Players.all()) {
			if (isInAutoMinerRegion(p)) {
				list.add(p);
			}
		}
		return list;
	}

	private boolean isInAutoMinerRegion(Player p) {
		return p.getWorld().getName().equals(this.world.getName()) && region.contains(p.getLocation());
	}

	public void stopAutoMinerTask() {
		this.autoMinerTask.stop();
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public void setRegion(IWrappedRegion region) {
		this.region = region;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public void setRewardPeriod(int rewardPeriod) {
		this.rewardPeriod = rewardPeriod;
	}

	public void setBlocksBroken(int blocksBroken) {
		this.blocksBroken = blocksBroken;
	}
}
