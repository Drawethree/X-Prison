package me.drawethree.wildprisoncore.gems.api;

import org.bukkit.OfflinePlayer;

public interface WildPrisonGemsAPI {

    long getPlayerTokens(OfflinePlayer p);

    boolean hasEnough(OfflinePlayer p, long amount);

    void removeTokens(OfflinePlayer p, long amount);

    void addTokens(OfflinePlayer p, long amount);


}
