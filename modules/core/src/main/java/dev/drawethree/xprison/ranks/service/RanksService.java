package dev.drawethree.xprison.ranks.service;

import org.bukkit.OfflinePlayer;

public interface RanksService {

	int getPlayerRank(OfflinePlayer player);

	void setRank(OfflinePlayer player, int rank);

	void createRank(OfflinePlayer player);
}
