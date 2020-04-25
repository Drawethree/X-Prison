package me.drawethree.wildprisonmultipliers.api;

import org.bukkit.entity.Player;

public interface WildPrisonMultipliersAPI {


    double getGlobalMultiplier();

    double getVoteMultiplier(Player p);

    double getRankMultiplier(Player p);

    double getPlayerMultiplier(Player p);

    default double getTotalToDeposit(Player p, double deposit) {
        return deposit * (1 + getPlayerMultiplier(p));
    }
}
