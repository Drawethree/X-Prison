package me.drawethree.wildprisoncore.multipliers.api;

import org.bukkit.entity.Player;

public interface WildPrisonMultipliersAPI {


    double getGlobalMultiplier();

    double getVoteMultiplier(Player p);

    double getRankMultiplier(Player p);

    double getPlayerMultiplier(Player p);

    default double getTotalToDeposit(Player p, double deposit) {
        return deposit * (1 + getPlayerMultiplier(p));
    }

    double getPrestigeMultiplier(Player p);
}
