package me.drawethree.wildprisonrankup.api;

import me.drawethree.wildprisonrankup.rank.Prestige;
import me.drawethree.wildprisonrankup.rank.Rank;
import org.bukkit.entity.Player;

public interface WildPrisonRankupAPI {

    Rank getPlayerRank(Player p);
    Prestige getPlayerPrestige(Player p);
}
