package dev.drawethree.xprison.gems.repo;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface GemsRepository {

	long getPlayerGems(OfflinePlayer player);

	void updateGems(OfflinePlayer player, long newAmount);

	Map<UUID, Long> getTopGems(int amountOfRecords);

	void addIntoGems(OfflinePlayer player, long startingGems);

	void createTables();

	void clearTableData();
}
