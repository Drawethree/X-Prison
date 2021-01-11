package me.drawethree.ultraprisoncore.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;

import static me.drawethree.ultraprisoncore.placeholders.UltraPrisonPlaceholder.formatNumber;

public class UltraPrisonMVdWPlaceholder {

	public UltraPrisonMVdWPlaceholder(UltraPrisonCore plugin) {

		if (!plugin.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier", event -> String.format("%.2f", plugin.getMultipliers().getApi().getPlayerMultiplier(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier_global", event -> String.format("%.2f", plugin.getMultipliers().getApi().getGlobalMultiplier()));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_rank", event -> plugin.getRanks().getApi().getPlayerRank(event.getPlayer()).getPrefix());

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank", event -> {
			Rank nextRank = plugin.getRanks().getApi().getNextPlayerRank(event.getPlayer());
			return nextRank == null ? "" : nextRank.getPrefix();
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_prestige", event -> plugin.getRanks().getApi().getPlayerPrestige(event.getPlayer()).getPrefix());

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_autominer_time", event -> plugin.getAutoMiner().getTimeLeft(event.getPlayer()));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_formatted", event -> formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_formatted", event -> formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_rankup_progress", event -> String.format("%d%%", plugin.getRanks().getRankManager().getRankupProgress(event.getPlayer())));
	}

}
