package dev.drawethree.ultraprisoncore.history.api;

import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.history.model.HistoryLine;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface UltraPrisonHistoryAPI {

	/**
	 * Gets players history
	 *
	 * @param player Player
	 * @return List containing all HistoryLine.class of Player
	 */
	List<HistoryLine> getPlayerHistory(OfflinePlayer player);

	/**
	 * Creates a new history line for player
	 *
	 * @param player  Player
	 * @param context Context of the history
	 * @param module  UltraPrisonModule associated with the history
	 */
	void createHistoryLine(OfflinePlayer player, UltraPrisonModule module, String context);
}
