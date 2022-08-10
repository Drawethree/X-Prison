package dev.drawethree.ultraprisoncore.tokens.task;

import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;

import java.util.concurrent.TimeUnit;

public final class UpdateTopBlocksTask implements Runnable {

	private final UltraPrisonTokens plugin;
	private Task task;

	public UpdateTopBlocksTask(UltraPrisonTokens plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		Players.all().forEach(p -> this.plugin.getTokensManager().savePlayerData(p, false, false));
		int amountOfRecords = this.plugin.getTokensConfig().getTopPlayersAmount();
		this.plugin.getCore().debug("Updating Blocks - Top " + amountOfRecords, this.plugin);
		this.plugin.getTokensManager().updateBlocksTop(this.plugin.getCore().getPluginDatabase().getTopBlocks(amountOfRecords));
		this.plugin.getCore().debug("Blocks Top Updated!", this.plugin);
	}

	public void start() {
		stop();
		this.task = Schedulers.async().runRepeating(this, 30, TimeUnit.SECONDS, this.plugin.getTokensConfig().getTopUpdateInterval(), TimeUnit.MINUTES);
	}

	public void stop() {
		if (task != null) {
			task.stop();
		}
	}
}
