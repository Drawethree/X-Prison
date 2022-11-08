package dev.drawethree.ultraprisoncore.tokens.repo;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public interface BlocksRepository {

	void resetBlocksWeekly(CommandSender sender);

	void updateBlocks(OfflinePlayer player, long newAmount);

	void updateBlocksWeekly(OfflinePlayer player, long newAmount);

	long getPlayerBrokenBlocksWeekly(OfflinePlayer player);

	void addIntoBlocks(OfflinePlayer player);

	void addIntoBlocksWeekly(OfflinePlayer player);

	long getPlayerBrokenBlocks(OfflinePlayer player);

	Map<UUID, Long> getTopBlocksWeekly(int amountOfRecords);

	Map<UUID, Long> getTopBlocks(int amountOfRecords);


}
