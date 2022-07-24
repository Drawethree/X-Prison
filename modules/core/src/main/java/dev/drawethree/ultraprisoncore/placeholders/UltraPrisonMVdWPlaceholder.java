package dev.drawethree.ultraprisoncore.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.autominer.utils.AutoMinerUtils;
import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.GlobalMultiplier;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.Multiplier;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import dev.drawethree.ultraprisoncore.pickaxelevels.UltraPrisonPickaxeLevels;
import dev.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.ranks.model.Rank;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import dev.drawethree.ultraprisoncore.utils.misc.MathUtils;
import dev.drawethree.ultraprisoncore.utils.misc.TimeUtil;

import java.util.Optional;

public class UltraPrisonMVdWPlaceholder {

	private final UltraPrisonCore plugin;

	public UltraPrisonMVdWPlaceholder(UltraPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void register() {
		this.registerTokensPlaceholders();
		this.registerEnchantsPlaceholders();
		this.registerGemsPlaceholders();
		this.registerGangsPlaceholders();
		this.registerMultipliersPlaceholders();
		this.registerRanksPlaceholders();
		this.registerPrestigesPlaceholders();
		this.registerPickaxeLevelsPlaceholders();
		this.registerAutoSellPlaceholders();
		this.registerMinesPlaceholders();
	}

	private void registerEnchantsPlaceholders() {
		if (!this.plugin.isModuleEnabled(UltraPrisonEnchants.MODULE_NAME)) {
			return;
		}
	}

	private void registerPrestigesPlaceholders() {

		if (!this.plugin.isModuleEnabled(UltraPrisonPrestiges.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_prestige", event -> plugin.getPrestiges().getApi().getPlayerPrestige(event.getPlayer()).getPrefix());
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_prestige_id", event -> String.valueOf(plugin.getPrestiges().getApi().getPlayerPrestige(event.getPlayer()).getId()));
	}

	private void registerAutoSellPlaceholders() {

		if (!this.plugin.isModuleEnabled(UltraPrisonAutoSell.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_autominer_time", event -> {
			int autominerTime = plugin.getAutoMiner().getManager().getAutoMinerTime(event.getPlayer());
			return AutoMinerUtils.getAutoMinerTimeLeftFormatted(autominerTime);
		});

	}

	private void registerPickaxeLevelsPlaceholders() {

		if (!this.plugin.isModuleEnabled(UltraPrisonPickaxeLevels.MODULE_NAME)) {
			return;
		}

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

	private void registerGangsPlaceholders() {

		if (!this.plugin.isModuleEnabled(UltraPrisonGangs.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_name", event -> {
			Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> this.plugin.getGangs().getPlaceholder("gang-in-gang").replace("%gang%", gang.getName())).orElseGet(() -> this.plugin.getGangs().getPlaceholder("gang-without"));
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang", event -> {
			Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> this.plugin.getGangs().getPlaceholder("gang-in-gang").replace("%gang%", gang.getName())).orElseGet(() -> this.plugin.getGangs().getPlaceholder("gang-without"));
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
			return optionalGang.map(gang -> gang.isOwner(event.getPlayer()) ? "Yes" : "No").orElse("");
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
			// +1 because of leader
			return optionalGang.map(gang -> String.valueOf(gang.getMembersOffline().size() + 1)).orElse("");
		});
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gang_members_online", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> String.valueOf(gang.getOnlinePlayers().size())).orElse("");
		});
	}

	private void registerRanksPlaceholders() {

		if (!this.plugin.isModuleEnabled(UltraPrisonRanks.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_rank", event -> plugin.getRanks().getApi().getPlayerRank(event.getPlayer()).getPrefix());

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank", event -> {
			Rank nextRank = plugin.getRanks().getApi().getNextPlayerRank(event.getPlayer());
			return nextRank == null ? "" : nextRank.getPrefix();
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_rankup_progress", event -> String.format("%d%%", plugin.getRanks().getRankManager().getRankupProgress(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_rankup_progress_bar", event -> plugin.getRanks().getRankManager().getRankupProgressBar(event.getPlayer()));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank_cost_raw", event -> String.valueOf(plugin.getRanks().getRankManager().getNextRankCost(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank_cost", event -> String.format("%,.2f", plugin.getRanks().getRankManager().getNextRankCost(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_next_rank_cost_formatted", event -> MathUtils.formatNumber(plugin.getRanks().getRankManager().getNextRankCost(event.getPlayer())));
	}

	private void registerMultipliersPlaceholders() {
		if (!this.plugin.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier_sell", event -> {
			PlayerMultiplier sellMulti = plugin.getMultipliers().getApi().getSellMultiplier(event.getPlayer());
			if (sellMulti == null || sellMulti.isExpired()) {
				return String.format("%.2f", 0.0);
			} else {
				return String.format("%.2f", (1.0 + sellMulti.getMultiplier()));
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier_token", event -> {
			PlayerMultiplier tokenMulti = plugin.getMultipliers().getApi().getTokenMultiplier(event.getPlayer());
			if (tokenMulti == null || tokenMulti.isExpired()) {
				return String.format("%.2f", 0.0);
			} else {
				return String.format("%.2f", (1.0 + tokenMulti.getMultiplier()));
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier_global_sell", event -> {
			GlobalMultiplier sellMulti = plugin.getMultipliers().getApi().getGlobalSellMultiplier();
			return String.format("%.2f", sellMulti.isExpired() ? 0.0 : sellMulti.getMultiplier());
		});
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier_global_token", event -> {
			GlobalMultiplier tokenMulti = plugin.getMultipliers().getApi().getGlobalTokenMultiplier();
			return String.format("%.2f", tokenMulti.isExpired() ? 0.0 : tokenMulti.getMultiplier());
		});

		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_multiplier_rank", event -> {
			Multiplier rankMulti = plugin.getMultipliers().getApi().getRankMultiplier(event.getPlayer());
			if (rankMulti == null) {
				return String.format("%.2f", 0.0);
			} else {
				return String.format("%.2f", rankMulti.getMultiplier());
			}
		});
	}

	private void registerGemsPlaceholders() {
		if (!this.plugin.isModuleEnabled(UltraPrisonGems.MODULE_NAME)) {
			return;
		}
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_1", event -> String.valueOf(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_2", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_3", event -> MathUtils.formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_gems_formatted", event -> MathUtils.formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
	}

	private void registerTokensPlaceholders() {
		if (!this.plugin.isModuleEnabled(UltraPrisonTokens.MODULE_NAME)) {
			return;
		}
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_formatted", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_1", event -> String.valueOf(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_2", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_3", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_formatted", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_1", event -> String.valueOf(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_2", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_blocks_3", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_tokens_formatted", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

	}

	private void registerMinesPlaceholders() {

		if (!this.plugin.isModuleEnabled(UltraPrisonMines.MODULE_NAME)) {
			return;
		}

		for (Mine mine : this.plugin.getMines().getManager().getMines()) {
			PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_mine_" + mine.getName() + "_blocks_left", event -> String.format("%,d", mine.getCurrentBlocks()));
			PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_mine_" + mine.getName() + "_blocks_left_percentage", event -> String.format("%,.2f", (double) mine.getCurrentBlocks() / mine.getTotalBlocks() * 100.0D));
			PlaceholderAPI.registerPlaceholder(plugin, "ultraprison_mine_" + mine.getName() + "_reset_time", event -> TimeUtil.getTime(mine.getSecondsToNextReset()));
		}
	}

}
