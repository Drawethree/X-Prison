package dev.drawethree.xprison.history.service;

import dev.drawethree.xprison.history.model.HistoryLineImpl;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface HistoryService {

	List<HistoryLineImpl> getPlayerHistory(OfflinePlayer player);

	void createHistoryLine(OfflinePlayer player, HistoryLineImpl history);

	void deleteHistory(OfflinePlayer target);
}
