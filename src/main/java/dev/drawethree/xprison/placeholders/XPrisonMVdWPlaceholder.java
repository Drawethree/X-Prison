package dev.drawethree.xprison.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.autominer.utils.AutoMinerUtils;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.Mine;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import dev.drawethree.xprison.multipliers.multiplier.GlobalMultiplier;
import dev.drawethree.xprison.multipliers.multiplier.Multiplier;
import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplier;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.pickaxelevels.model.PickaxeLevel;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.ranks.model.Rank;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.misc.MathUtils;
import dev.drawethree.xprison.utils.misc.TimeUtil;

import java.util.Optional;

public class XPrisonMVdWPlaceholder {

	private final XPrison plugin;

	public XPrisonMVdWPlaceholder(XPrison plugin) {
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
		if (!this.plugin.isModuleEnabled(XPrisonEnchants.MODULE_NAME)) {
			return;
		}
	}

	private void registerPrestigesPlaceholders() {

		if (!this.plugin.isModuleEnabled(XPrisonPrestiges.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_prestige", event -> plugin.getPrestiges().getApi().getPlayerPrestige(event.getPlayer()).getPrefix());
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_prestige_id", event -> String.valueOf(plugin.getPrestiges().getApi().getPlayerPrestige(event.getPlayer()).getId()));
	}

	private void registerAutoSellPlaceholders() {

		if (!this.plugin.isModuleEnabled(XPrisonAutoSell.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_autominer_time", event -> {
			int autominerTime = plugin.getAutoMiner().getManager().getAutoMinerTime(event.getPlayer());
			return AutoMinerUtils.getAutoMinerTimeLeftFormatted(autominerTime);
		});

	}

	private void registerPickaxeLevelsPlaceholders() {

		if (!this.plugin.isModuleEnabled(XPrisonPickaxeLevels.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_pickaxe_level", event -> {
			Optional<PickaxeLevel> levelOptional = plugin.getPickaxeLevels().getApi().getPickaxeLevel(event.getPlayer());
			return levelOptional.map(level -> String.valueOf(level.getLevel())).orElse("0");
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_pickaxe_progress", event -> plugin.getPickaxeLevels().getApi().getProgressBar(event.getPlayer()));
	}

	private void registerGangsPlaceholders() {

		if (!this.plugin.isModuleEnabled(XPrisonGangs.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_name", event -> {
			Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> this.plugin.getGangs().getConfig().getPlaceholder("gang-in-gang").replace("%gang%", gang.getName())).orElseGet(() -> this.plugin.getGangs().getConfig().getPlaceholder("gang-without"));
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang", event -> {
			Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> this.plugin.getGangs().getConfig().getPlaceholder("gang-in-gang").replace("%gang%", gang.getName())).orElseGet(() -> this.plugin.getGangs().getConfig().getPlaceholder("gang-without"));
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_value", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return String.format("%,d", optionalGang.get().getValue());
			} else {
				return "";
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_has_gang", event -> plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer()).isPresent() ? "Yes" : "No");
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_is_leader", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> gang.isOwner(event.getPlayer()) ? "Yes" : "No").orElse("");
		});
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_leader_name", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			if (optionalGang.isPresent()) {
				return optionalGang.get().getOwnerOffline().getName();
			}
			return "";
		});
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_members_amount", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			// +1 because of leader
			return optionalGang.map(gang -> String.valueOf(gang.getMembersOffline().size() + 1)).orElse("");
		});
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gang_members_online", event -> {
			Optional<Gang> optionalGang = plugin.getGangs().getGangsManager().getPlayerGang(event.getPlayer());
			return optionalGang.map(gang -> String.valueOf(gang.getOnlinePlayers().size())).orElse("");
		});
	}

	private void registerRanksPlaceholders() {

		if (!this.plugin.isModuleEnabled(XPrisonRanks.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_rank", event -> plugin.getRanks().getApi().getPlayerRank(event.getPlayer()).getPrefix());

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_next_rank", event -> {
			Optional<Rank> nextRank = plugin.getRanks().getApi().getNextPlayerRank(event.getPlayer());
			if (!nextRank.isPresent()) {
				return "";
			} else {
				return nextRank.get().getPrefix();
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_rankup_progress", event -> String.format("%d%%", plugin.getRanks().getRanksManager().getRankupProgress(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_rankup_progress_bar", event -> plugin.getRanks().getRanksManager().getRankupProgressBar(event.getPlayer()));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_next_rank_cost_raw", event -> String.valueOf(plugin.getRanks().getRanksManager().getNextRankCost(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_next_rank_cost", event -> String.format("%,.2f", plugin.getRanks().getRanksManager().getNextRankCost(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_next_rank_cost_formatted", event -> MathUtils.formatNumber(plugin.getRanks().getRanksManager().getNextRankCost(event.getPlayer())));
	}

	private void registerMultipliersPlaceholders() {
		if (!this.plugin.isModuleEnabled(XPrisonMultipliers.MODULE_NAME)) {
			return;
		}

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_multiplier_sell", event -> {
			PlayerMultiplier sellMulti = plugin.getMultipliers().getApi().getSellMultiplier(event.getPlayer());
			if (sellMulti == null || sellMulti.isExpired()) {
				return String.format("%.2f", 0.0);
			} else {
				return String.format("%.2f", (1.0 + sellMulti.getMultiplier()));
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_multiplier_token", event -> {
			PlayerMultiplier tokenMulti = plugin.getMultipliers().getApi().getTokenMultiplier(event.getPlayer());
			if (tokenMulti == null || tokenMulti.isExpired()) {
				return String.format("%.2f", 0.0);
			} else {
				return String.format("%.2f", (1.0 + tokenMulti.getMultiplier()));
			}
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_multiplier_global_sell", event -> {
			GlobalMultiplier sellMulti = plugin.getMultipliers().getApi().getGlobalSellMultiplier();
			return String.format("%.2f", sellMulti.isExpired() ? 0.0 : sellMulti.getMultiplier());
		});
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_multiplier_global_token", event -> {
			GlobalMultiplier tokenMulti = plugin.getMultipliers().getApi().getGlobalTokenMultiplier();
			return String.format("%.2f", tokenMulti.isExpired() ? 0.0 : tokenMulti.getMultiplier());
		});

		PlaceholderAPI.registerPlaceholder(plugin, "xprison_multiplier_rank", event -> {
			Multiplier rankMulti = plugin.getMultipliers().getApi().getRankMultiplier(event.getPlayer());
			if (rankMulti == null) {
				return String.format("%.2f", 0.0);
			} else {
				return String.format("%.2f", rankMulti.getMultiplier());
			}
		});
	}

	private void registerGemsPlaceholders() {
		if (!this.plugin.isModuleEnabled(XPrisonGems.MODULE_NAME)) {
			return;
		}
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gems_1", event -> String.valueOf(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gems_2", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gems_3", event -> MathUtils.formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gems", event -> String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_gems_formatted", event -> MathUtils.formatNumber(plugin.getGems().getGemsManager().getPlayerGems(event.getPlayer())));
	}

	private void registerTokensPlaceholders() {
		if (!this.plugin.isModuleEnabled(XPrisonTokens.MODULE_NAME)) {
			return;
		}
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_tokens", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_tokens_formatted", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_tokens_1", event -> String.valueOf(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_tokens_2", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_tokens_3", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_blocks", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_blocks_formatted", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_blocks_1", event -> String.valueOf(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_blocks_2", event -> String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_blocks_3", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(event.getPlayer())));
		PlaceholderAPI.registerPlaceholder(plugin, "xprison_tokens_formatted", event -> MathUtils.formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(event.getPlayer())));

	}

	private void registerMinesPlaceholders() {

		if (!this.plugin.isModuleEnabled(XPrisonMines.MODULE_NAME)) {
			return;
		}

		for (Mine mine : this.plugin.getMines().getManager().getMines()) {
			PlaceholderAPI.registerPlaceholder(plugin, "xprison_mine_" + mine.getName() + "_blocks_left", event -> String.format("%,d", mine.getCurrentBlocks()));
			PlaceholderAPI.registerPlaceholder(plugin, "xprison_mine_" + mine.getName() + "_blocks_left_percentage", event -> String.format("%,.2f", (double) mine.getCurrentBlocks() / mine.getTotalBlocks() * 100.0D));
			PlaceholderAPI.registerPlaceholder(plugin, "xprison_mine_" + mine.getName() + "_reset_time", event -> TimeUtil.getTime(mine.getSecondsToNextReset()));
		}
	}

}
