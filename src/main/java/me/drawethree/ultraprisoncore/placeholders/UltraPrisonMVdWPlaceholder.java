package me.drawethree.ultraprisoncore.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;

import static me.drawethree.ultraprisoncore.placeholders.UltraPrisonPlaceholder.formatNumber;

public class UltraPrisonMVdWPlaceholder {

    public UltraPrisonMVdWPlaceholder(UltraPrisonCore plugin) {

        if (!plugin.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            return;
        }

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_formatted", event -> formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_1", event -> String.valueOf(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_2", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_3", event -> formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_1", event -> String.valueOf(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_2", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_3", event -> formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_formatted", event -> formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_1", event -> String.valueOf(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_2", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_3", event -> formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));


        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));


        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier", event -> String.format("%.2f", (1.0 + plugin.getMultipliers().getApi().getPlayerMultiplier(event.getPlayer()))));

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
        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank_cost", event -> String.format("%,.2f", plugin.getRanks().getRankManager().getNextRankCost(event.getPlayer())));

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_pickaxe_level", event -> {
            PickaxeLevel level = plugin.getPickaxeLevels().getApi().getPickaxeLevel(event.getPlayer());
            if (level != null) {
                return String.valueOf(level.getLevel());
            } else {
                return "0";
            }
        });

        PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_pickaxe_progress", event -> plugin.getPickaxeLevels().getProgressBar(event.getPlayer()));
    }

}
