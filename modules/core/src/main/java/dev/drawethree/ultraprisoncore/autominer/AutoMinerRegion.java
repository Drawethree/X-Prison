package dev.drawethree.ultraprisoncore.autominer;

import dev.drawethree.ultraprisoncore.autominer.api.events.PlayerAutomineEvent;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class AutoMinerRegion {

	private final UltraPrisonAutoMiner parent;
	private World world;
	private IWrappedRegion region;
	private List<String> commands;
	private int rewardPeriod;
	private int blocksBroken;
	private Task autoMinerTask;

	public AutoMinerRegion(UltraPrisonAutoMiner parent, World world, IWrappedRegion region, List<String> rewards, int rewardPeriod, int blocksBroken) {
		this.parent = parent;
		this.world = world;
		this.region = region;
		this.commands = rewards;
		this.rewardPeriod = rewardPeriod;
		this.blocksBroken = blocksBroken;
	}

	public void startAutoMinerTask() {

		AtomicInteger counter = new AtomicInteger();

		this.autoMinerTask = Schedulers.async().runRepeating(() -> {

			int current = counter.getAndIncrement();
			boolean resetCounterAfterEnd = current >= rewardPeriod;

			List<Player> players = this.getPlayersInRegion();
			for (Player p : players) {

				if (!this.executeTaskValidationAndNotifyPlayerOnFail(p)) {
					continue;
				}

				this.decrementPlayerAutoMinerTimeAndNotify(p);

				if (resetCounterAfterEnd) {
					this.executeTaskLogic(p);
				}
			}

			if (resetCounterAfterEnd) {
				counter.set(0);
			}

		}, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
	}

	private void decrementPlayerAutoMinerTimeAndNotify(Player p) {
		this.parent.getManager().decrementPlayerAutominerTime(p);
		parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_enabled"));
	}

	private boolean executeTaskValidationAndNotifyPlayerOnFail(Player p) {

		if (!this.checkPlayerAutoMinerTime(p)) {
			parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_disabled"));
			return false;
		}

		if (!this.checkPlayerItemInHand(p)) {
			parent.getCore().getNmsProvider().sendActionBar(p, parent.getMessage("auto_miner_no_pickaxe"));
			return false;
		}

		return true;
	}

	private List<Player> getPlayersInRegion() {
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

	private void executeTaskLogic(Player p) {

		if (!this.callAutoMineEvent(p)) {
			return;
		}

		this.executeCommands(p);
		this.parent.getCore().getEnchants().getEnchantsManager().addBlocksBrokenToItem(p, p.getItemInHand(), this.blocksBroken);
	}

	private boolean callAutoMineEvent(Player p) {
		PlayerAutomineEvent event = this.parent.getManager().callAutoMineEvent(p);
		return event.isCancelled();
	}

	private boolean checkPlayerItemInHand(Player p) {
		ItemStack item = p.getItemInHand();
		return this.parent.getCore().isPickaxeSupported(item);
	}

	private boolean checkPlayerAutoMinerTime(Player p) {
		return parent.getManager().hasAutoMinerTime(p);
	}

	public void stopAutoMinerTask() {
		if (autoMinerTask != null) {
			autoMinerTask.close();
		}
	}

	private void executeCommands(Player p) {
		if (this.commands.isEmpty()) {
			return;
		}
		this.commands.forEach(c -> Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()))));
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
