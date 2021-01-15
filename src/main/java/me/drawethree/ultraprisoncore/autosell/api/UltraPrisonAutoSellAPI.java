package me.drawethree.ultraprisoncore.autosell.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

public interface UltraPrisonAutoSellAPI {

    /**
     * Method to get current earnings of player
     *
     * @param player Player
     * @return Current earnings
     */
    double getCurrentEarnings(Player player);

    /**
     * Method to get price for broken block in specified mine region
     * @param region IWrappedRegion
     * @param block Block
     * @return Price for broken block
     */
    double getPriceForBrokenBlock(IWrappedRegion region, Block block);

    /**
     * Method to get if player has autosell enabled
     * @param p Player
     * @return true if player has autosell enabled, otherwise false
     */
    boolean hasAutoSellEnabled(Player p);
}
