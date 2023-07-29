package dev.drawethree.xprison.gems.api;

import dev.drawethree.xprison.api.enums.ReceiveCause;
import org.bukkit.OfflinePlayer;

public interface XPrisonGemsAPI {

	/**
	 * Method to get player gems
	 *
	 * @param p Player
	 * @return Player gems amount
	 */
	long getPlayerGems(OfflinePlayer p);

	/**
	 * Method to check if player has more or equal gems than specified amount
	 *
	 * @param p      Player
	 * @param amount amount
	 * @return true if player has more or equal gems
	 */
	boolean hasEnough(OfflinePlayer p, long amount);

	/**
	 * Method to remove gems from player
	 *
	 * @param p      Player
	 * @param amount amount
	 */
	void removeGems(OfflinePlayer p, long amount);

	/**
	 * Method to add gems to player
	 *
	 * @param p      Player
	 * @param amount amount
	 * @param cause  - Represents why player get these gemes
	 */
	void addGems(OfflinePlayer p, long amount, ReceiveCause cause);


}
