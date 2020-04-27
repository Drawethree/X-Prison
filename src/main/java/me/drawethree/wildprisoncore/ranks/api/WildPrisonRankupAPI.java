package me.drawethree.wildprisoncore.ranks.api;


import me.drawethree.wildprisoncore.ranks.rank.Prestige;
import me.drawethree.wildprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;

public interface WildPrisonRankupAPI {

    Rank getPlayerRank(Player p);
    Prestige getPlayerPrestige(Player p);
}
