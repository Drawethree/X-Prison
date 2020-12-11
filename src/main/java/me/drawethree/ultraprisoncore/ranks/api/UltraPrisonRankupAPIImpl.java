package me.drawethree.ultraprisoncore.ranks.api;

import me.drawethree.ultraprisoncore.ranks.UltraPrisonRankup;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;

public class UltraPrisonRankupAPIImpl implements UltraPrisonRankupAPI{

    private UltraPrisonRankup plugin;

    public UltraPrisonRankupAPIImpl(UltraPrisonRankup plugin) {
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
