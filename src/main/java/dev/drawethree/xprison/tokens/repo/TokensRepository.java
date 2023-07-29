package dev.drawethree.xprison.tokens.repo;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface TokensRepository {

	long getPlayerTokens(OfflinePlayer player);

	void updateTokens(OfflinePlayer player, long newAmount);

	Map<UUID, Long> getTopTokens(int amountOfRecords);

	void addIntoTokens(OfflinePlayer player, long startingTokens);

	void createTables();

	void clearTableData();
}
