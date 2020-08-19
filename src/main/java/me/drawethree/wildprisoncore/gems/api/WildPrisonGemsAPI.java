package me.drawethree.wildprisoncore.gems.api;

import org.bukkit.OfflinePlayer;

public interface WildPrisonGemsAPI {

	long getPlayerGems(OfflinePlayer p);

    boolean hasEnough(OfflinePlayer p, long amount);

	void removeGems(OfflinePlayer p, long amount);

	void addGems(OfflinePlayer p, long amount);


}
