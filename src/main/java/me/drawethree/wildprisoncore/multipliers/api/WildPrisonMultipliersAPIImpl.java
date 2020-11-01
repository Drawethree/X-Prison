package me.drawethree.wildprisoncore.multipliers.api;

import me.drawethree.wildprisoncore.multipliers.WildPrisonMultipliers;
import org.bukkit.entity.Player;

public class WildPrisonMultipliersAPIImpl implements WildPrisonMultipliersAPI {

    private WildPrisonMultipliers plugin;

    public WildPrisonMultipliersAPIImpl(WildPrisonMultipliers plugin) {

        this.plugin = plugin;
    }

    @Override
    public double getGlobalMultiplier() {
        return plugin.getGlobalMultiplier();
    }

    @Override
    public double getVoteMultiplier(Player p) {
        return plugin.getPersonalMultiplier(p);
    }

    @Override
    public double getRankMultiplier(Player p) {
        return plugin.getRankMultiplier(p);
    }

    @Override
    public double getPlayerMultiplier(Player p) {
        return this.getGlobalMultiplier() + this.getVoteMultiplier(p) + this.getRankMultiplier(p) + this.getPrestigeMultiplier(p);
    }

    @Override
    public double getPrestigeMultiplier(Player p) {
        return plugin.getPrestigeMultiplier(p);
    }
}
