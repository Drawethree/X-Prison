package me.drawethree.ultraprisoncore.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import org.bukkit.ChatColor;

import java.util.Optional;

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
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_prestige_id", event -> String.valueOf(plugin.getRanks().getApi().getPlayerPrestige(event.getPlayer()).getId()));


		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_autominer_time", event -> plugin.getAutoMiner().getTimeLeft(event.getPlayer()));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_formatted", event -> formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_formatted", event -> formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_rankup_progress", event -> String.format("%d%%", plugin.getRanks().getRankManager().getRankupProgress(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank_cost", event -> String.format("%,.2f", plugin.getRanks().getRankManager().getNextRankCost(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank_cost_formatted", event -> formatNumber(plugin.getRanks().getRankManager().getNextRankCost(event.getPlayer())));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_pickaxe_level", event -> {
			PickaxeLevel level = plugin.getPickaxeLevels().getApi().getPickaxeLevel(event.getPlayer());
			if (level != null) {
				return String.valueOf(level.getLevel());
			} else {
				return "0";
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_pickaxe_progress", event -> plugin.getPickaxeLevels().getProgressBar(event.getPlayer()));

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_name", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return optionalGang.get().getName();
			} else {
				return ChatColor.RED + "✗";
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_value", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return String.format("%,d", optionalGang.get().getValue());
			} else {
				return "";
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_has_gang", event -> plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer()).isPresent() ? "Yes" : "No");
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_is_leader", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return optionalGang.get().isOwner(event.getPlayer()) ? "Yes" : "No";
			}
			return "";
		});
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_leader_name", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return optionalGang.get().getOwnerOffline().getName();
			}
			return "";
		});
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_members_amount", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return String.valueOf(optionalGang.get().getMembersOffline().size());
			}
			return "";
		});
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_members_online", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return String.valueOf(optionalGang.get().getOnlinePlayers().size());
			}
			return "";
		});
	}

}
