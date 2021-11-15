package me.drawethree.ultraprisoncore.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.multipliers.multiplier.GlobalMultiplier;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import me.drawethree.ultraprisoncore.ranks.model.Rank;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
public class UltraPrisonPAPIPlaceholder extends PlaceholderExpansion {

	private UltraPrisonCore plugin;

	/**
	 * Since we register the expansion inside our own plugin, we
	 * can simply use this method here to get an instance of our
	 * plugin.
	 *
	 * @param plugin The instance of our plugin.
	 */
	public UltraPrisonPAPIPlaceholder(UltraPrisonCore plugin) {
		this.plugin = plugin;
	}

	/**
	 * Because this is an internal class,
	 * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
	 * PlaceholderAPI is reloaded
	 *
	 * @return true to persist through reloads
	 */
	@Override
	public boolean persist() {
		return true;
	}

	/**
	 * Because this is a internal class, this check is not needed
	 * and we can simply return {@code true}
	 *
	 * @return Always true since it's an internal class.
	 */
	@Override
	public boolean canRegister() {
		return true;
	}

	/**
	 * The name of the person who created this expansion should go here.
	 * <br>For convienience do we return the author from the plugin.yml
	 *
	 * @return The name of the author as a String.
	 */
	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	/**
	 * The placeholder identifier should go here.
	 * <br>This is what tells PlaceholderAPI to call our onRequest
	 * method to obtain a value if a placeholder starts with our
	 * identifier.
	 * <br>This must be unique and can not contain % or _
	 *
	 * @return The identifier in {@code %<identifier>_<value>%} as String.
	 */
	@Override
	public String getIdentifier() {
		return "ultraprison";
	}

	/**
	 * This is the version of the expansion.
	 * <br>You don't have to use numbers, since it is set as a String.
	 * <p>
	 * For convienience do we return the version from the plugin.yml
	 *
	 * @return The version as a String.
	 */
	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	/**
	 * This is the method called when a placeholder with our identifier
	 * is found and needs a value.
	 * <br>We specify the value identifier in this method.
	 * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
	 *
	 * @param player
	 * @param identifier A String containing the identifier/value.
	 * @return possibly-null String of the requested identifier.
	 */
	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

		if (player == null) {
			return null;
		}

		if (identifier.startsWith("mine_")) {

			String mineName = identifier.replace("mine_","").split("_")[0];
			Mine mine = plugin.getMines().getManager().getMineByName(mineName);

			if (mine == null) {
				return null;
			}

			String placeholder = identifier.replace("mine_" + mineName + "_","");
			switch (placeholder.toLowerCase()) {
				case "blocks_left": {
					return String.format("%,d", mine.getCurrentBlocks());
				}
				case "blocks_left_percentage": {
					return String.format("%,.2f", (double) mine.getCurrentBlocks() / mine.getTotalBlocks() * 100.0D);
				}
			}
		}

		switch (identifier.toLowerCase()) {
			case "tokens":
			case "tokens_2":
				return String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(player));
			case "gems":
			case "gems_2":
				return String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(player));
			case "blocks":
			case "blocks_2":
				return String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(player));
			case "multiplier_sell": {
				PlayerMultiplier sellMulti = plugin.getMultipliers().getApi().getSellMultiplier(player);
				if (sellMulti == null || sellMulti.isExpired()) {
					return String.format("%.2f", 0.0);
				} else {
					return String.format("%.2f", (1.0 + sellMulti.getMultiplier()));
				}
			}
			case "multiplier_token": {
				PlayerMultiplier tokenMulti = plugin.getMultipliers().getApi().getTokenMultiplier(player);
				if (tokenMulti == null || tokenMulti.isExpired()) {
					return String.format("%.2f", 0.0);
				} else {
					return String.format("%.2f", (1.0 + tokenMulti.getMultiplier()));
				}
			}
			case "multiplier_global_sell": {
				GlobalMultiplier sellMulti = plugin.getMultipliers().getApi().getGlobalSellMultiplier();
				return String.format("%.2f", sellMulti.isExpired() ? 0.0 : sellMulti.getMultiplier());
			}
			case "multiplier_global_token": {
				GlobalMultiplier tokenMulti = plugin.getMultipliers().getApi().getGlobalTokenMultiplier();
				return String.format("%.2f", tokenMulti.isExpired() ? 0.0 : tokenMulti.getMultiplier());
			}
			case "rank":
				return plugin.getRanks().getApi().getPlayerRank(player).getPrefix();
			case "next_rank": {
				Rank nextRank = plugin.getRanks().getApi().getNextPlayerRank(player);
				return nextRank == null ? "" : nextRank.getPrefix();
			}
			case "next_rank_cost_raw":
				return String.valueOf(plugin.getRanks().getRankManager().getNextRankCost(player));
			case "next_rank_cost":
				return String.format("%,.2f", plugin.getRanks().getRankManager().getNextRankCost(player));
			case "next_rank_cost_formatted":
				return formatNumber(plugin.getRanks().getRankManager().getNextRankCost(player));
			case "prestige":
				return plugin.getPrestiges().getApi().getPlayerPrestige(player).getPrefix();
			case "prestige_id":
				return String.valueOf(plugin.getPrestiges().getApi().getPlayerPrestige(player).getId());
			case "autominer_time":
				return plugin.getAutoMiner().getTimeLeft(player);
			case "tokens_formatted":
			case "tokens_3":
				return formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(player));
			case "gems_formatted":
			case "gems_3":
				return formatNumber(plugin.getGems().getGemsManager().getPlayerGems(player));
			case "rankup_progress":
				return String.format("%d%%", plugin.getRanks().getRankManager().getRankupProgress(player));
			case "rankup_progress_bar":
				return plugin.getRanks().getRankManager().getRankupProgressBar(player);
			case "tokens_1":
				return String.valueOf(plugin.getTokens().getTokensManager().getPlayerTokens(player));
			case "blocks_1":
				return String.valueOf(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(player));
			case "blocks_3":
				return formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(player));
			case "gems_1":
				return String.valueOf(plugin.getGems().getGemsManager().getPlayerGems(player));
			case "pickaxe_level": {
				PickaxeLevel level = plugin.getPickaxeLevels().getApi().getPickaxeLevel(player);

				if (level != null) {
					return String.valueOf(level.getLevel());
				} else {
					return "0";
				}
			}
			case "pickaxe_progress":
				return this.plugin.getPickaxeLevels().getProgressBar(player);
			case "gang_name":
			case "gang": {
				Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
				return optionalGang.map(gang -> this.plugin.getGangs().getPlaceholder("gang-in-gang").replace("%gang%", gang.getName())).orElseGet(() -> this.plugin.getGangs().getPlaceholder("gang-without"));
			}
			case "gang_value": {
				Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
				if (optionalGang.isPresent()) {
					return String.format("%,d", optionalGang.get().getValue());
				} else {
					return "";
				}
			}
			case "gang_has_gang":
				return this.plugin.getGangs().getGangsManager().getPlayerGang(player).isPresent() ? "Yes" : "No";
			case "gang_is_leader": {
				Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
				return optionalGang.map(gang -> gang.isOwner(player) ? "Yes" : "No").orElse("");
			}
			case "gang_leader_name": {
				Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
				if (optionalGang.isPresent()) {
					return optionalGang.get().getOwnerOffline().getName();
				}
				return "";
			}
			case "gang_members_amount": {
				Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
				// +1 because of leader
				return optionalGang.map(gang -> String.valueOf(gang.getMembersOffline().size() + 1)).orElse("");
			}
			case "gang_members_online": {
				Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
				return optionalGang.map(gang -> String.valueOf(gang.getOnlinePlayers().size())).orElse("");
			}
			default:
				return null;
		}
	}

	static String formatNumber(double amount) {
		if (amount <= 1000.0D)
			return String.valueOf(amount);
		ArrayList<String> suffixes = new ArrayList<>(Arrays.asList("", "k", "M", "B", "T", "q", "Q", "QT", "S", "SP", "O",
				"N", "D"));
		double chunks = Math.floor(Math.floor(Math.log10(amount) / 3.0D));
		amount /= Math.pow(10.0D, chunks * 3.0D - 1.0D);
		amount /= 10.0D;
		String suffix = suffixes.get((int) chunks);
		String format = String.valueOf(amount);
		if (format.replace(".", "").length() > 5)
			format = format.substring(0, 5);
		return format + suffix;
	}
}