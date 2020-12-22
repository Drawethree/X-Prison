package me.drawethree.ultraprisoncore.ranks.api;


import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;

public interface UltraPrisonRankupAPI {

    Rank getPlayerRank(Player p);

	int getPlayerPrestige(Player p);

	default String getPrestigePrefix(int id) {
		return Text.colorize(String.format("%,d", id));
	}

    Rank getNextPlayerRank(Player player);
}
