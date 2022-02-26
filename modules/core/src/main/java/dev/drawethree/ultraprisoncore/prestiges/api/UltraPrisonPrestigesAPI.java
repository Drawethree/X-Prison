package dev.drawethree.ultraprisoncore.prestiges.api;


import dev.drawethree.ultraprisoncore.prestiges.model.Prestige;
import org.bukkit.entity.Player;

public interface UltraPrisonPrestigesAPI {

	/**
	 * Method to get player Prestige
	 *
	 * @param p Player
	 * @return Prestige
	 */
	Prestige getPlayerPrestige(Player p);

	/**
	 * Sets a prestige to online player
	 *
	 * @param player   Player
	 * @param prestige Prestige
	 */
	void setPlayerPrestige(Player player, Prestige prestige);

	/**
	 * Sets a prestige to online player
	 *
	 * @param player   Player
	 * @param prestige Prestige
	 */
	void setPlayerPrestige(Player player, long prestige);

}
