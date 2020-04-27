package me.drawethree.wildprisonrankup.api;

import me.drawethree.wildprisonrankup.WildPrisonRankup;
import me.drawethree.wildprisonrankup.rank.Prestige;
import me.drawethree.wildprisonrankup.rank.Rank;
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
    public Prestige getPlayerPrestige(Player p) {
        return plugin.getRankManager().getPlayerPrestige(p);
    }
}
