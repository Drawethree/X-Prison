package me.drawethree.ultraprisoncore.database;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
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

	public abstract void updateRankAndPrestige(OfflinePlayer player, int rank, long prestige);

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

	public abstract void addIntoTokens(OfflinePlayer player);

	public abstract void addIntoBlocks(OfflinePlayer player);

	public abstract void addIntoBlocksWeekly(OfflinePlayer player);

	public abstract void addIntoGems(OfflinePlayer player);

	public abstract void addIntoRanksAndPrestiges(OfflinePlayer player);

	public abstract List<Gang> getAllGangs();

	public abstract void updateGang(Gang g);

	public abstract void deleteGang(Gang g);

	public abstract void createGang(Gang g);

	public abstract void updatePlayerNickname(OfflinePlayer player);

	public abstract void saveSellMultiplier(Player player, PlayerMultiplier multiplier);

	public abstract void saveTokenMultiplier(Player player, PlayerMultiplier multiplier);

	public abstract PlayerMultiplier getSellMultiplier(Player player);

	public abstract PlayerMultiplier getTokenMultiplier(Player player);
}
