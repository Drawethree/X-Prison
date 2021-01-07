package me.drawethree.ultraprisoncore.autosell.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

public interface UltraPrisonAutoSellAPI {

    double getCurrentEarnings(Player player);

    double getPriceForBrokenBlock(IWrappedRegion region, Block block);

    boolean hasAutoSellEnabled(Player p);
}
