package dev.drawethree.xprison.autominer.model;

import com.cryptomorin.xseries.messages.ActionBar;
import dev.drawethree.xprison.autominer.api.events.PlayerAutomineEvent;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class AutoMinerTask implements Runnable {

	private final AtomicInteger counter;
	private final AutoMinerRegion region;
	private Task task;

	public AutoMinerTask(AutoMinerRegion region) {
		this.region = region;
		this.counter = new AtomicInteger(0);
	}

	@Override
	public void run() {

		int current = counter.getAndIncrement();
		boolean resetCounterAfterEnd = current >= region.getRewardPeriod();

		List<Player> players = this.region.getPlayersInRegion();

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
	}

	public void start() {
		this.stop();
		this.task = Schedulers.async().runRepeating(this, 1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
	}

	public void stop() {
		if (this.task != null) {
			this.task.stop();
		}
	}

	private void decrementPlayerAutoMinerTimeAndNotify(Player p) {
		this.region.getPlugin().getManager().decrementPlayerAutominerTime(p);
		ActionBar.sendActionBar(p, this.region.getPlugin().getAutoMinerConfig().getMessage("auto_miner_enabled"));
	}

	private boolean executeTaskValidationAndNotifyPlayerOnFail(Player p) {

		if (!this.checkPlayerAutoMinerTime(p)) {
			ActionBar.sendActionBar(p, this.region.getPlugin().getAutoMinerConfig().getMessage("auto_miner_disabled"));
			return false;
		}

		if (!this.checkPlayerItemInHand(p)) {
			ActionBar.sendActionBar(p, this.region.getPlugin().getAutoMinerConfig().getMessage("auto_miner_no_pickaxe"));
			return false;
		}

		return true;
	}

	private void executeTaskLogic(Player p) {

		if (!this.callAutoMineEvent(p)) {
			return;
		}

		this.executeCommands(p);
		this.region.getPlugin().getCore().getEnchants().getEnchantsManager().addBlocksBrokenToItem(p, p.getItemInHand(), this.region.getBlocksBroken());
	}

	private boolean callAutoMineEvent(Player p) {
		PlayerAutomineEvent event = this.region.getPlugin().getManager().callAutoMineEvent(p);
		return !event.isCancelled();
	}

	private boolean checkPlayerItemInHand(Player p) {
		ItemStack item = p.getItemInHand();
		return this.region.getPlugin().getCore().isPickaxeSupported(item);
	}

	private boolean checkPlayerAutoMinerTime(Player p) {
		return this.region.getPlugin().getManager().hasAutoMinerTime(p);
	}

	private void executeCommands(Player p) {

		if (this.region.getCommands().isEmpty()) {
			return;
		}

		this.region.getCommands().forEach(c -> Schedulers.sync().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", p.getName()))));
	}
}
