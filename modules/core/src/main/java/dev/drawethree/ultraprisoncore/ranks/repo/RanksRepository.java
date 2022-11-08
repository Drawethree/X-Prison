package dev.drawethree.ultraprisoncore.ranks.repo;

import org.bukkit.OfflinePlayer;

public interface RanksRepository {

	int getPlayerRank(OfflinePlayer player);

	void updateRank(OfflinePlayer player, int rank);

	void addIntoRanks(OfflinePlayer player);


}
