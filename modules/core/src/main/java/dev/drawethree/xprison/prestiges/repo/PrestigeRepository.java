package dev.drawethree.xprison.prestiges.repo;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface PrestigeRepository {

	void updatePrestige(OfflinePlayer player, long prestige);

	void addIntoPrestiges(OfflinePlayer player);

	long getPlayerPrestige(OfflinePlayer player);

	Map<UUID, Long> getTopPrestiges(int amountOfRecords);

	void createTables();

	void clearTableData();
}
