package me.drawethree.ultraprisoncore.autosell.api;

import me.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

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
    public long getPriceForBrokenBlock(IWrappedRegion region, Block block) {
        return plugin.getPriceForBrokenBlock(region, block);
    }

    @Override
    public boolean hasAutoSellEnabled(Player p) {
        return plugin.hasAutoSellEnabled(p);
    }
}
