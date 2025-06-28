package dev.drawethree.xprison.multipliers.service;

import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplierBase;
import org.bukkit.entity.Player;

public interface MultipliersService {

	void setSellMultiplier(Player player, PlayerMultiplierBase multiplier);

	void deleteSellMultiplier(Player player);

	void setTokenMultiplier(Player player, PlayerMultiplierBase multiplier);

	void deleteTokenMultiplier(Player player);

	PlayerMultiplierBase getSellMultiplier(Player player);

	PlayerMultiplierBase getTokenMultiplier(Player player);

	void removeExpiredMultipliers();
}
