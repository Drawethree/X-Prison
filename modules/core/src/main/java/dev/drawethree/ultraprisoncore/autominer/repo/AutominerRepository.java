package dev.drawethree.ultraprisoncore.autominer.repo;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface AutominerRepository {

	int getPlayerAutoMinerTime(OfflinePlayer player);

	void removeExpiredAutoMiners();

	void saveAutoMiner(Player p, int timeLeft);

	void createTables();

	void clearTableData();
}
