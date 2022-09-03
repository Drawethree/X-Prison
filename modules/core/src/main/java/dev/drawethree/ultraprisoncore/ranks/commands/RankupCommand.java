package dev.drawethree.ultraprisoncore.ranks.commands;

import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import me.lucko.helper.Commands;

public class RankupCommand {

	private static final String[] COMMAND_ALIASES = {"rankup"};
	private final UltraPrisonRanks plugin;

	public RankupCommand(UltraPrisonRanks plugin) {
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
