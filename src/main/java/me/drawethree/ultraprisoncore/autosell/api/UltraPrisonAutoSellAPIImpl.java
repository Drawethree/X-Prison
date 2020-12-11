package me.drawethree.ultraprisoncore.autosell.api;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class UltraPrisonAutoSellAPIImpl implements UltraPrisonAutoSellAPI {

    private UltraPrisonAutoSell plugin;

    public UltraPrisonAutoSellAPIImpl(UltraPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    @Override
    public double getCurrentEarnings(Player player) {
        return plugin.getCurrentEarnings(player);
    }

    @Override
    public long getPriceForBrokenBlock(ProtectedRegion region, Block block) {
        return plugin.getPriceForBrokenBlock(region, block);
    }

    @Override
    public boolean hasAutoSellEnabled(Player p) {
        return plugin.hasAutoSellEnabled(p);
    }
}
