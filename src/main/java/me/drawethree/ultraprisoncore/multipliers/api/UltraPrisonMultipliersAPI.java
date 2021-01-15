package me.drawethree.ultraprisoncore.multipliers.api;

import org.bukkit.entity.Player;

public interface UltraPrisonMultipliersAPI {


    /**
     * Method to get current global multiplier
     *
     * @return global multiplier value
     */
    double getGlobalMultiplier();

    /**
     * Method to get player's vote multiplier
     * @param p Player
     * @return vote multiplier
     */
    double getVoteMultiplier(Player p);

    /**
     * Method to get player's rank multiplier
     * @param p Player
     * @return rank multiplier
     */
    double getRankMultiplier(Player p);

    /**
     * Method to get overall player's multiplier
     * @param p Player
     * @return overall player's multiplier
     */
    double getPlayerMultiplier(Player p);

    /**
     * Method to calculate total amount to deposit
     * @param p Player
     * @param deposit original amount to deposit
     * @return new amount to deposit
     */
    default double getTotalToDeposit(Player p, double deposit) {
        return deposit * (1 + getPlayerMultiplier(p));
    }

}
