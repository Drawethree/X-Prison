package dev.drawethree.xprison.multipliers.service;

import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.entity.Player;

public interface MultipliersService {

	void setSellMultiplier(Player player, PlayerMultiplier multiplier);

	void deleteSellMultiplier(Player player);

	void setTokenMultiplier(Player player, PlayerMultiplier multiplier);

	void deleteTokenMultiplier(Player player);

	PlayerMultiplier getSellMultiplier(Player player);

	PlayerMultiplier getTokenMultiplier(Player player);

	void removeExpiredMultipliers();
}
