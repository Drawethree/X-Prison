package dev.drawethree.xprison.ranks.service.impl;

import dev.drawethree.xprison.ranks.repo.RanksRepository;
import dev.drawethree.xprison.ranks.service.RanksService;
import org.bukkit.OfflinePlayer;

public class RanksServiceImpl implements RanksService {

	private final RanksRepository repository;

	public RanksServiceImpl(RanksRepository repository) {
		this.repository = repository;
	}

	@Override
	public int getPlayerRank(OfflinePlayer player) {
		return repository.getPlayerRank(player);
	}

	@Override
	public void setRank(OfflinePlayer player, int rank) {
		repository.updateRank(player, rank);
	}

	@Override
	public void createRank(OfflinePlayer player) {
		repository.addIntoRanks(player);
	}
}
