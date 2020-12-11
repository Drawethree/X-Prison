package me.drawethree.ultraprisoncore.gems.api;

import org.bukkit.OfflinePlayer;

public interface UltraPrisonGemsAPI {

	long getPlayerGems(OfflinePlayer p);

    boolean hasEnough(OfflinePlayer p, long amount);

	void removeGems(OfflinePlayer p, long amount);

	void addGems(OfflinePlayer p, long amount);


}
