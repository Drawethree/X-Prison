package dev.drawethree.ultraprisoncore.history.repo;

import dev.drawethree.ultraprisoncore.history.model.HistoryLine;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface HistoryRepository {

	List<HistoryLine> getPlayerHistory(OfflinePlayer player);

	void addHistoryLine(OfflinePlayer player, HistoryLine history);

	void clearHistory(OfflinePlayer target);
}
