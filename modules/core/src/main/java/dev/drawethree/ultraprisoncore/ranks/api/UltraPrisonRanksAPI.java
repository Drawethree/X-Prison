package dev.drawethree.ultraprisoncore.ranks.api;


import dev.drawethree.ultraprisoncore.ranks.model.Rank;
import org.bukkit.entity.Player;

public interface UltraPrisonRanksAPI {

	/**
	 * Method to get player Rank
	 *
	 * @param p Player
	 * @return Rank
	 */
	Rank getPlayerRank(Player p);

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

}
