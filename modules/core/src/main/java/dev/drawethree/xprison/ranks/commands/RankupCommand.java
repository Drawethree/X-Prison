package dev.drawethree.xprison.ranks.commands;

import dev.drawethree.xprison.ranks.XPrisonRanks;
import me.lucko.helper.Commands;

public class RankupCommand {

	private static final String[] COMMAND_ALIASES = {"rankup"};
	private final XPrisonRanks plugin;

	public RankupCommand(XPrisonRanks plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						this.plugin.getRanksManager().buyNextRank(c.sender());
					}
				}).registerAndBind(this.plugin.getCore(), COMMAND_ALIASES);
	}
}
