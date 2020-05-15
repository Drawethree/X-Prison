package me.drawethree.wildprisoncore.autosell.api;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface WildPrisonAutoSellAPI {

    double getCurrentEarnings(Player player);

    long getPriceForBrokenBlock(ProtectedRegion region, Block block);

    boolean hasAutoSellEnabled(Player p);
}
