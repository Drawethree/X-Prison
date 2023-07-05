package dev.drawethree.xprison.history.service.impl;

import dev.drawethree.xprison.history.model.HistoryLine;
import dev.drawethree.xprison.history.repo.HistoryRepository;
import dev.drawethree.xprison.history.service.HistoryService;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class HistoryServiceImpl implements HistoryService {

	private final HistoryRepository repository;

	public HistoryServiceImpl(HistoryRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		return repository.getPlayerHistory(player);
	}

	@Override
	public void createHistoryLine(OfflinePlayer player, HistoryLine history) {
		repository.addHistoryLine(player, history);
	}

	@Override
	public void deleteHistory(OfflinePlayer target) {
		repository.deleteHistory(target);
	}
}
