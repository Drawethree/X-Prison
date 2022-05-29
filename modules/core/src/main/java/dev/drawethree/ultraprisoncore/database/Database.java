package dev.drawethree.ultraprisoncore.database;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.history.model.HistoryLine;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Database {

	protected final UltraPrisonCore plugin;

	Database(UltraPrisonCore plugin) {
		this.plugin = plugin;
	}

	public abstract DatabaseType getDatabaseType();

	public abstract void createTables();

	public abstract void createIndexes();

	public abstract long getPlayerTokens(OfflinePlayer player);

	public abstract long getPlayerGems(OfflinePlayer player);

	public abstract long getPlayerBrokenBlocks(OfflinePlayer player);

	public abstract int getPlayerRank(OfflinePlayer player);

	public abstract long getPlayerPrestige(OfflinePlayer player);

	public abstract int getPlayerAutoMinerTime(OfflinePlayer player);

	public abstract void updateTokens(OfflinePlayer player, long newAmount);

	public abstract boolean resetAllData();

	public abstract boolean resetData(UltraPrisonModule module);

	public abstract void resetBlocksWeekly(CommandSender sender);

	public abstract void updateGems(OfflinePlayer player, long newAmount);

	public abstract void updateRank(OfflinePlayer player, int rank);

	public abstract void updatePrestige(OfflinePlayer player, long prestige);

	public abstract void removeExpiredAutoMiners();

	public abstract void removeExpiredMultipliers();

	public abstract void updateBlocks(OfflinePlayer player, long newAmount);

	public abstract void updateBlocksWeekly(OfflinePlayer player, long newAmount);

	public abstract long getPlayerBrokenBlocksWeekly(OfflinePlayer player);

	public abstract void saveAutoMiner(Player p, int timeLeft);

	public abstract Map<UUID, Integer> getTop10Prestiges();

	public abstract Map<UUID, Long> getTop10Gems();

	public abstract Map<UUID, Long> getTop10BlocksWeekly();

	public abstract Map<UUID, Long> getTop10Tokens();

	public abstract Map<UUID, Long> getTop10Blocks();

	public abstract void addIntoTokens(OfflinePlayer player, long startingTokens);

	public abstract void addIntoBlocks(OfflinePlayer player);

	public abstract void addIntoBlocksWeekly(OfflinePlayer player);

	public abstract void addIntoGems(OfflinePlayer player, long startingGems);

	public abstract void addIntoRanks(OfflinePlayer player);

	public abstract void addIntoPrestiges(OfflinePlayer player);

	public abstract List<Gang> getAllGangs();

	public abstract void updateGang(Gang g);

	public abstract void deleteGang(Gang g);

	public abstract void createGang(Gang g);

	public abstract void updatePlayerNickname(OfflinePlayer player);

	public abstract void saveSellMultiplier(Player player, PlayerMultiplier multiplier);

	public abstract void deleteSellMultiplier(Player player);

	public abstract void saveTokenMultiplier(Player player, PlayerMultiplier multiplier);

	public abstract void deleteTokenMultiplier(Player player);

	public abstract PlayerMultiplier getSellMultiplier(Player player);

	public abstract PlayerMultiplier getTokenMultiplier(Player player);

	public abstract List<HistoryLine> getPlayerHistory(OfflinePlayer player);

	public abstract void addHistoryLine(OfflinePlayer player, HistoryLine history);

	public abstract void clearHistory(OfflinePlayer target);
}
