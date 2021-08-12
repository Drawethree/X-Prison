package me.drawethree.ultraprisoncore.ranks.api;


import me.drawethree.ultraprisoncore.ranks.rank.Prestige;
import me.drawethree.ultraprisoncore.ranks.rank.Rank;
import org.bukkit.entity.Player;

public interface UltraPrisonRankupAPI {

	/**
	 * Method to get player Rank
	 *
	 * @param p Player
	 * @return Rank
	 */
	Rank getPlayerRank(Player p);

	/**
	 * Method to get player Prestige
	 *
	 * @param p Player
	 * @return Prestige
	 */
	Prestige getPlayerPrestige(Player p);

	/**
	 * Method to get next player rank
	 *
	 * @param player Player
	 * @return null if he has max rank, otherwise next Rank
	 */
	Rank getNextPlayerRank(Player player);

	/**
	 * Method to get player's rankup progress
	 *
	 * @param player Player
	 * @return int 0-100 percentage
	 */
	int getRankupProgress(Player player);

	/**
	 * Sets a rank to online player
	 *
	 * @param player Player
	 * @param rank   Rank
	 */
	void setPlayerRank(Player player, Rank rank);


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
