package dev.drawethree.xprison.bombs.handler;

import org.bukkit.block.Block;

import java.util.List;

public interface BlockHandler {

    List<Block> handle(List<Block> originalBlocks);
}