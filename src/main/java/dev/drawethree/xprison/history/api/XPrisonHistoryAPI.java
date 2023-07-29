package dev.drawethree.xprison.history.api;

import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.history.model.HistoryLine;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface XPrisonHistoryAPI {

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
	 * @param module  XPrisonModule associated with the history
	 */
	void createHistoryLine(OfflinePlayer player, XPrisonModule module, String context);
}
