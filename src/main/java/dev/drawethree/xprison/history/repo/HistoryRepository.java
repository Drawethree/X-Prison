package dev.drawethree.xprison.history.repo;

import dev.drawethree.xprison.history.model.HistoryLineImpl;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface HistoryRepository {

	List<HistoryLineImpl> getPlayerHistory(OfflinePlayer player);

	void addHistoryLine(OfflinePlayer player, HistoryLineImpl history);

	void deleteHistory(OfflinePlayer target);

	void createTables();

	void clearTableData();
}
