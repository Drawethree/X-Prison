package me.drawethree.wildprisoncore.autosell.api;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface WildPrisonAutoSellAPI {

    long getCurrentEarnings(Player player);

    int getPriceForBrokenBlock(ProtectedRegion region, Block block);

    boolean hasAutoSellEnabled(Player p);
}
