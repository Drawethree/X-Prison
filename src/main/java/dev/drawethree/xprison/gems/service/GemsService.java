package dev.drawethree.xprison.gems.service;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface GemsService {

	long getPlayerGems(OfflinePlayer player);

	void setGems(OfflinePlayer player, long newAmount);

	Map<UUID, Long> getTopGems(int amountOfRecords);

	void createGems(OfflinePlayer player, long startingGems);
}
