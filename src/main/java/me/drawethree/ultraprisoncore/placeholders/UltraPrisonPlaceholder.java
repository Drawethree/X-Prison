package me.drawethree.ultraprisoncore.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.drawethree.ultraprisoncore.pickaxelevels.model.PickaxeLevel;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
public class UltraPrisonPlaceholder extends PlaceholderExpansion {

    private UltraPrisonCore plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin The instance of our plugin.
     */
    public UltraPrisonPlaceholder(UltraPrisonCore plugin) {
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
            return "";
        }


        if (identifier.equalsIgnoreCase("tokens") || identifier.equalsIgnoreCase("tokens_2")) {
            return String.format("%,d", plugin.getTokens().getTokensManager().getPlayerTokens(player));
        } else if (identifier.equalsIgnoreCase("gems") || identifier.equalsIgnoreCase("gems_2")) {
            return String.format("%,d", plugin.getGems().getGemsManager().getPlayerGems(player));
        } else if (identifier.equalsIgnoreCase("blocks") || identifier.equalsIgnoreCase("blocks_2")) {
            return String.format("%,d", plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(player));
        } else if (identifier.equalsIgnoreCase("multiplier")) {
            return String.format("%.2f", (1.0 + plugin.getMultipliers().getApi().getPlayerMultiplier(player)));
        } else if (identifier.equalsIgnoreCase("multiplier_global")) {
            return String.format("%.2f", plugin.getMultipliers().getApi().getGlobalMultiplier());
        } else if (identifier.equalsIgnoreCase("rank")) {
            return plugin.getRanks().getApi().getPlayerRank(player).getPrefix();
        } else if (identifier.equalsIgnoreCase("next_rank")) {
            Rank nextRank = plugin.getRanks().getApi().getNextPlayerRank(player);
            return nextRank == null ? "" : nextRank.getPrefix();
        } else if (identifier.equalsIgnoreCase("next_rank_cost")) {
            return String.format("%,.2f", plugin.getRanks().getRankManager().getNextRankCost(player));
        } else if (identifier.equalsIgnoreCase("prestige")) {
            return plugin.getRanks().getApi().getPlayerPrestige(player).getPrefix();
        } else if (identifier.equalsIgnoreCase("autominer_time")) {
            return plugin.getAutoMiner().getTimeLeft(player);
        } else if (identifier.equalsIgnoreCase("tokens_formatted") || identifier.equalsIgnoreCase("tokens_3")) {
            return formatNumber(plugin.getTokens().getTokensManager().getPlayerTokens(player));
        } else if (identifier.equalsIgnoreCase("gems_formatted")) {
            return formatNumber(plugin.getGems().getGemsManager().getPlayerGems(player));
        } else if (identifier.equalsIgnoreCase("rankup_progress")) {
            return String.format("%d%%", plugin.getRanks().getRankManager().getRankupProgress(player));
        } else if (identifier.equalsIgnoreCase("tokens_1")) {
            return String.valueOf(plugin.getTokens().getTokensManager().getPlayerTokens(player));
        } else if (identifier.equalsIgnoreCase("blocks_1")) {
            return String.valueOf(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(player));
        } else if (identifier.equalsIgnoreCase("blocks_3")) {
            return formatNumber(plugin.getTokens().getTokensManager().getPlayerBrokenBlocks(player));
        } else if (identifier.equalsIgnoreCase("gems_1")) {
            return String.valueOf(plugin.getGems().getGemsManager().getPlayerGems(player));
        } else if (identifier.equalsIgnoreCase("gems_3")) {
            return formatNumber(plugin.getGems().getGemsManager().getPlayerGems(player));
        } else if (identifier.equalsIgnoreCase("pickaxe_level")) {

            PickaxeLevel level = plugin.getPickaxeLevels().getApi().getPickaxeLevel(player);

            if (level != null) {
                return String.valueOf(level.getLevel());
            } else {
                return "0";
            }

        } else if (identifier.equalsIgnoreCase("pickaxe_progress")) {
            return this.plugin.getPickaxeLevels().getProgressBar(player);
        } else if (identifier.equalsIgnoreCase("gang_name")) {
            Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
            if (optionalGang.isPresent()) {
                return optionalGang.get().getName();
            } else {
                return "";
            }
        } else if (identifier.equalsIgnoreCase("gang_value")) {
            Optional<Gang> optionalGang = this.plugin.getGangs().getGangsManager().getPlayerGang(player);
            if (optionalGang.isPresent()) {
                return String.format("%,d", optionalGang.get().getValue());
            } else {
                return "";
            }
        }
        return null;
    }

    public static String formatNumber(double amount) {
        if (amount <= 1000.0D)
            return String.valueOf(amount);
        ArrayList<String> suffixes = new ArrayList<>(Arrays.asList(new String[]{
                "", "k", "M", "B", "T", "q", "Q", "QT", "S", "SP", "O",
                "N", "D"}));
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