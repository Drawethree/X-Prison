package dev.drawethree.xprison.prestiges.service;

import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface PrestigeService {

	void setPrestige(OfflinePlayer player, long prestige);

	void createPrestige(OfflinePlayer player);

	long getPlayerPrestige(OfflinePlayer player);

	Map<UUID, Long> getTopPrestiges(int amountOfRecords);
}
