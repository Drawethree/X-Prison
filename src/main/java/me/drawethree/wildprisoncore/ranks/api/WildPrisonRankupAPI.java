package me.drawethree.wildprisoncore.ranks.api;


import me.drawethree.wildprisoncore.ranks.rank.Rank;
import me.lucko.helper.text.Text;
import org.bukkit.entity.Player;

public interface WildPrisonRankupAPI {

    Rank getPlayerRank(Player p);

	int getPlayerPrestige(Player p);

	default String getPrestigePrefix(int id) {
		return Text.colorize(String.format("&cP%,d", id));
	}
}
