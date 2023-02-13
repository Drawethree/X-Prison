package dev.drawethree.xprison.tokens.api;

import dev.drawethree.xprison.api.enums.LostCause;
import dev.drawethree.xprison.api.enums.ReceiveCause;
import org.bukkit.OfflinePlayer;

public interface XPrisonTokensAPI {


	/**
	 * Method to get player's tokens
	 *
	 * @param p Player
	 * @return amount of player's tokens
	 */
	long getPlayerTokens(OfflinePlayer p);

	/**
	 * Method to check if player has more or equal tokens than specified amount
	 *
	 * @param p      Player
	 * @param amount amount
	 * @return true if player has more or equal tokens than amount
	 */
	boolean hasEnough(OfflinePlayer p, long amount);

	/**
	 * Method to remove tokens from player
	 *
	 * @param p      Player
	 * @param amount amount
	 */
	void removeTokens(OfflinePlayer p, long amount, LostCause cause);

	/**
	 * Method to add tokens to player
	 *
	 * @param p      Player
	 * @param amount amount
	 * @param cause  - Represents why player get these tokens
	 */
	void addTokens(OfflinePlayer p, long amount, ReceiveCause cause);


}
