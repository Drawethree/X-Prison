package me.drawethree.ultraprisoncore.multipliers.api;

import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import org.bukkit.entity.Player;

public class UltraPrisonMultipliersAPIImpl implements UltraPrisonMultipliersAPI {

    private UltraPrisonMultipliers plugin;

    public UltraPrisonMultipliersAPIImpl(UltraPrisonMultipliers plugin) {

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
        return this.getGlobalMultiplier() + this.getVoteMultiplier(p) + this.getRankMultiplier(p);
    }

}
