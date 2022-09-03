package dev.drawethree.ultraprisoncore.ranks.commands;

import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import me.lucko.helper.Commands;

public class MaxRankupCommand {

	private static final String[] COMMAND_ALIASES = {"maxrankup", "mru"};
	private static final String PERMISSION_REQUIRED = "ultraprison.ranks.maxrankup";
	private final UltraPrisonRanks plugin;

	public MaxRankupCommand(UltraPrisonRanks plugin) {
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
