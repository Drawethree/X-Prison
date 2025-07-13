package dev.drawethree.xprison.multipliers.multiplier;

import org.bukkit.OfflinePlayer;

/**
 * Represents a multiplier specifically tied to a player.
 * Extends the general {@link Multiplier} interface.
 */
public interface PlayerMultiplier extends Multiplier {

    /**
     * Gets the {@link OfflinePlayer} associated with this multiplier.
     *
     * @return the offline player
     */
    OfflinePlayer getOfflinePlayer();
}
