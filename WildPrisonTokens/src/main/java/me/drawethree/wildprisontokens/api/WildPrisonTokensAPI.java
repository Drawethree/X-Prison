package me.drawethree.wildprisontokens.api;

import org.bukkit.OfflinePlayer;

public interface WildPrisonTokensAPI {

    long getPlayerTokens(OfflinePlayer p);

    boolean hasEnough(OfflinePlayer p, long amount);

    void removeTokens(OfflinePlayer p, long amount);

    void addTokens(OfflinePlayer p, long amount);


}
