package me.drawethree.ultraprisoncore.database;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public abstract class Database {

	protected final UltraPrisonCore plugin;

	public Database(UltraPrisonCore plugin) {
		this.plugin = plugin;
	}

	public abstract PlayerMultiplier getPlayerPersonalMultiplier(OfflinePlayer player);

	public abstract long getPlayerTokens(OfflinePlayer player);

	public abstract long getPlayerGems(OfflinePlayer player);

	public abstract long getPlayerBrokenBlocks(OfflinePlayer player);

	public abstract int getPlayerRank(OfflinePlayer player);

	public abstract int getPlayerPrestige(OfflinePlayer player);

	public abstract int getPlayerAutoMinerTime(OfflinePlayer player);

	public abstract void updateTokens(OfflinePlayer player, long newAmount);

	public abstract void resetAllData(CommandSender sender);

	public abstract void resetBlocksWeekly(CommandSender sender);

	public abstract void updateGems(OfflinePlayer player, long newAmount);

	public abstract void updateRankAndPrestige(OfflinePlayer player, int rank, int prestige);

	public abstract void removeExpiredAutoMiners();

	public abstract void removeExpiredMultipliers();

	public abstract void updateBlocks(OfflinePlayer player, long newAmount);

	public abstract void updateBlocksWeekly(OfflinePlayer player, long newAmount);

	public abstract long getPlayerBrokenBlocksWeekly(OfflinePlayer player);
}
