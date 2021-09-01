package me.drawethree.ultraprisoncore.gangs.api;

import me.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public interface UltraPrisonGangsAPI {

    /**
     * Method to get Gang from player
     * @param player OfflinePlayer
     * @return Optional<Gang> gang
     */
    Optional<Gang> getPlayerGang(OfflinePlayer player);

    /**
     * Method to get Gang from name
     * @param name name of gang
     * @return Optional<Gang> gang
     */
    Optional<Gang> getByName(String name);


}
