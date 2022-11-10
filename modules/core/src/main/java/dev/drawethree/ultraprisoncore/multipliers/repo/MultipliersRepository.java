package dev.drawethree.ultraprisoncore.multipliers.repo;

import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.entity.Player;

public interface MultipliersRepository {

	void saveSellMultiplier(Player player, PlayerMultiplier multiplier);

	void deleteSellMultiplier(Player player);

	void saveTokenMultiplier(Player player, PlayerMultiplier multiplier);

	void deleteTokenMultiplier(Player player);

	PlayerMultiplier getSellMultiplier(Player player);

	PlayerMultiplier getTokenMultiplier(Player player);

	void removeExpiredMultipliers();

	void createTables();

	void clearTableData();
}
