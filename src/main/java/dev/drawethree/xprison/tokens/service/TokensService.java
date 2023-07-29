package dev.drawethree.xprison.tokens.service;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface TokensService {

	long getTokens(OfflinePlayer player);

	void setTokens(OfflinePlayer player, long newAmount);

	Map<UUID, Long> getTopTokens(int amountOfRecords);

	void createTokens(OfflinePlayer player, long startingTokens);
}
