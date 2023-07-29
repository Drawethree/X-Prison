package dev.drawethree.xprison.prestiges.service.impl;

import dev.drawethree.xprison.prestiges.repo.PrestigeRepository;
import dev.drawethree.xprison.prestiges.service.PrestigeService;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public class PrestigeServiceImpl implements PrestigeService {

	private final PrestigeRepository repository;

	public PrestigeServiceImpl(PrestigeRepository repository) {
		this.repository = repository;
	}

	@Override
	public void setPrestige(OfflinePlayer player, long prestige) {
		repository.updatePrestige(player, prestige);
	}

	@Override
	public void createPrestige(OfflinePlayer player) {
		repository.addIntoPrestiges(player);
	}

	@Override
	public long getPlayerPrestige(OfflinePlayer player) {
		return repository.getPlayerPrestige(player);
	}

	@Override
	public Map<UUID, Long> getTopPrestiges(int amountOfRecords) {
		return repository.getTopPrestiges(amountOfRecords);
	}
}
