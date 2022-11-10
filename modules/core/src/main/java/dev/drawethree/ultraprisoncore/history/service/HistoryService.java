package dev.drawethree.ultraprisoncore.history.service;

import dev.drawethree.ultraprisoncore.history.model.HistoryLine;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface HistoryService {

	List<HistoryLine> getPlayerHistory(OfflinePlayer player);

	void createHistoryLine(OfflinePlayer player, HistoryLine history);

	void deleteHistory(OfflinePlayer target);
}
