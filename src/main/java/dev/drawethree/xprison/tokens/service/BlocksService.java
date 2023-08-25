package dev.drawethree.xprison.tokens.service;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface BlocksService {

	void resetBlocksWeekly();

	void setBlocks(OfflinePlayer player, long newAmount);

	void setBlocksWeekly(OfflinePlayer player, long newAmount);

	long getPlayerBrokenBlocksWeekly(OfflinePlayer player);

	void createBlocks(OfflinePlayer player);

	void createBlocksWeekly(OfflinePlayer player);

	long getPlayerBrokenBlocks(OfflinePlayer player);

	Map<UUID, Long> getTopBlocksWeekly(int amountOfRecords);

	Map<UUID, Long> getTopBlocks(int amountOfRecords);
}
