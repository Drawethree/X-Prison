package me.drawethree.wildprisoncore.ranks.api;

import me.drawethree.wildprisoncore.ranks.WildPrisonRankup;
import me.drawethree.wildprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;

public class WildPrisonRankupAPIImpl implements WildPrisonRankupAPI{

    private WildPrisonRankup plugin;

    public WildPrisonRankupAPIImpl(WildPrisonRankup plugin) {
        this.plugin = plugin;
    }

    @Override
    public Rank getPlayerRank(Player p) {
        return plugin.getRankManager().getPlayerRank(p);
    }

    @Override
	public int getPlayerPrestige(Player p) {
        return plugin.getRankManager().getPlayerPrestige(p);
    }
}
