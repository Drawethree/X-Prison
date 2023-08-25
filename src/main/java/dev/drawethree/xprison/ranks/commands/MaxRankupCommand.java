package dev.drawethree.xprison.ranks.commands;

import dev.drawethree.xprison.ranks.XPrisonRanks;
import me.lucko.helper.Commands;

public class MaxRankupCommand {

	private static final String[] COMMAND_ALIASES = {"maxrankup", "mru"};
	private static final String PERMISSION_REQUIRED = "xprison.ranks.maxrankup";
	private final XPrisonRanks plugin;

	public MaxRankupCommand(XPrisonRanks plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPermission(PERMISSION_REQUIRED, this.plugin.getRanksConfig().getMessage("no_permission"))
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getRanksManager().buyMaxRank(c.sender());
					}
				}).registerAndBind(this.plugin.getCore(), COMMAND_ALIASES);
	}
}
