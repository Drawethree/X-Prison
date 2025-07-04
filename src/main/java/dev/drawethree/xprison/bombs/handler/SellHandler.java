package dev.drawethree.xprison.bombs.handler;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface SellHandler {

    void sell(Player player, List<Block> blocks);

}