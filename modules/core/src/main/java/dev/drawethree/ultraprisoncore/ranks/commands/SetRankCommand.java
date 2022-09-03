package dev.drawethree.ultraprisoncore.ranks.commands;

import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.ranks.model.Rank;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SetRankCommand {

	private static final String PERMISSION_REQUIRED = "ultraprison.ranks.admin";
	private static final String[] COMMAND_ALIASES = {"setrank"};

	private final UltraPrisonRanks plugin;

	public SetRankCommand(UltraPrisonRanks plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPermission(PERMISSION_REQUIRED)
				.handler(c -> {
					if (c.args().size() == 2) {
						Player target = c.arg(0).parseOrFail(Player.class);
						Optional<Rank> rankOptional = this.plugin.getRanksManager().getRankById(c.arg(1).parseOrFail(Integer.class));

						if (rankOptional.isEmpty()) {
							PlayerUtils.sendMessage(c.sender(), "&cInvalid rank id provided.");
							return;
						}

						this.plugin.getRanksManager().setRank(target, rankOptional.get(), c.sender());
					}
				}).registerAndBind(this.plugin.getCore(), COMMAND_ALIASES);
	}
}
