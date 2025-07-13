package dev.drawethree.xprison.enchants.model;

import org.bukkit.event.block.BlockBreakEvent;

/**
 * Interface for enchantments that trigger special behavior on block break events.
 */
public interface BlockBreakEnchant {

    /**
     * Handles logic when a block is broken with an item that has this enchantment.
     *
     * @param event        the {@link BlockBreakEvent} triggered by breaking the block
     * @param enchantLevel the level of the enchantment on the tool
     */
    void onBlockBreak(BlockBreakEvent event, int enchantLevel);

}
