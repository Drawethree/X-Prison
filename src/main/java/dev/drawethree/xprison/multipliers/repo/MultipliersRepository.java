package dev.drawethree.xprison.multipliers.repo;

import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplierBase;
import org.bukkit.entity.Player;

public interface MultipliersRepository {

	void saveSellMultiplier(Player player, PlayerMultiplierBase multiplier);

	void deleteSellMultiplier(Player player);

	void saveTokenMultiplier(Player player, PlayerMultiplierBase multiplier);

	void deleteTokenMultiplier(Player player);

	PlayerMultiplierBase getSellMultiplier(Player player);

	PlayerMultiplierBase getTokenMultiplier(Player player);

	void removeExpiredMultipliers();

	void createTables();

	void clearTableData();
}
