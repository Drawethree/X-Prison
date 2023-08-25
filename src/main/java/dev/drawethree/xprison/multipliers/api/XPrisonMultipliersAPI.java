package dev.drawethree.xprison.multipliers.api;

import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import dev.drawethree.xprison.multipliers.multiplier.GlobalMultiplier;
import dev.drawethree.xprison.multipliers.multiplier.Multiplier;
import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.entity.Player;

public interface XPrisonMultipliersAPI {


	/**
	 * Method to get current global sell multiplier
	 *
	 * @return GlobalMultiplier
	 */
	GlobalMultiplier getGlobalSellMultiplier();

	/**
	 * Method to get current global token multiplier
	 *
	 * @return GlobalMultiplier
	 */
	GlobalMultiplier getGlobalTokenMultiplier();

	/**
	 * Method to get player's sell multiplier
	 *
	 * @param p Player
	 * @return PlayerMultiplier
	 */
	PlayerMultiplier getSellMultiplier(Player p);

	/**
	 * Method to get player's token multiplier
	 *
	 * @param p Player
	 * @return PlayerMultiplier
	 */
	PlayerMultiplier getTokenMultiplier(Player p);

	/**
	 * Method to get player's rank multiplier
	 *
	 * @param p Player
	 * @return Multiplier
	 */
	Multiplier getRankMultiplier(Player p);

	/**
	 * Method to get overall player's multiplier based on multiplier type (SELL / TOKENS)
	 *
	 * @param p              Player
	 * @param multiplierType MultiplierType
	 * @return overall player's multiplier
	 */
	double getPlayerMultiplier(Player p, MultiplierType multiplierType);

	/**
	 * Method to calculate total amount to deposit (tokens / money )
	 *
	 * @param p       Player
	 * @param deposit original amount to deposit
	 * @param type    MultiplierType
	 * @return new amount to deposit
	 */
	default double getTotalToDeposit(Player p, double deposit, MultiplierType type) {
		return deposit * (1.0 + this.getPlayerMultiplier(p, type));
	}

}
