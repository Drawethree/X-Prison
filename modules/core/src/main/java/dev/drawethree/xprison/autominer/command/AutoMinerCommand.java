package dev.drawethree.xprison.autominer.command;

import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.autominer.utils.AutoMinerUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.entity.Player;

public class AutoMinerCommand {

	private static final String[] COMMAND_ALIASES = {"autominer"};

	private final XPrisonAutoMiner plugin;

	public AutoMinerCommand(XPrisonAutoMiner plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {

					if (!validateArguments(c)) {
						return;
					}

					int timeLeft = this.plugin.getManager().getAutoMinerTime(c.sender());
					PlayerUtils.sendMessage(c.sender(), this.plugin.getAutoMinerConfig().getMessage("auto_miner_time").replace("%time%", AutoMinerUtils.getAutoMinerTimeLeftFormatted(timeLeft)));

				}).registerAndBind(this.plugin.getCore(), COMMAND_ALIASES);
	}

	private boolean validateArguments(CommandContext<Player> c) {
		return c.args().size() == 0;
	}
}
