package me.drawethree.ultraprisoncore.tokens.api;

import org.bukkit.OfflinePlayer;

public interface UltraPrisonTokensAPI {


    /**
     * Method to get player's tokens
     *
     * @param p Player
     * @return amount of player's tokens
     */
    long getPlayerTokens(OfflinePlayer p);

    /**
     * Method to check if player has more or equal tokens than specified amount
     * @param p Player
     * @param amount amount
     * @return true if player has more or equal tokens than amount
     */
    boolean hasEnough(OfflinePlayer p, long amount);

    /**
     * Method to remove tokens from player
     * @param p Player
     * @param amount amount
     */
    void removeTokens(OfflinePlayer p, long amount);

    /**
     * Method to add tokens to player
     * @param p Player
     * @param amount amount
     */
    void addTokens(OfflinePlayer p, long amount);


}
