package me.drawethree.ultraprisoncore.ranks.api;


import me.drawethree.ultraprisoncore.ranks.rank.Prestige;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;

public interface UltraPrisonRankupAPI {

    Rank getPlayerRank(Player p);

	Prestige getPlayerPrestige(Player p);

    Rank getNextPlayerRank(Player player);

	int getRankupProgress(Player player);

}
