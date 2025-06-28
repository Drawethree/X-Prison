package dev.drawethree.xprison.multipliers.service.impl;

import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplierBase;
import dev.drawethree.xprison.multipliers.repo.MultipliersRepository;
import dev.drawethree.xprison.multipliers.service.MultipliersService;
import org.bukkit.entity.Player;

public class MultipliersServiceImpl implements MultipliersService {

	private final MultipliersRepository repository;

	public MultipliersServiceImpl(MultipliersRepository repository) {
		this.repository = repository;
	}

	@Override
	public void setSellMultiplier(Player player, PlayerMultiplierBase multiplier) {
		this.repository.saveSellMultiplier(player, multiplier);
	}

	@Override
	public void deleteSellMultiplier(Player player) {
		this.repository.deleteSellMultiplier(player);
	}

	@Override
	public void setTokenMultiplier(Player player, PlayerMultiplierBase multiplier) {
		this.repository.saveTokenMultiplier(player, multiplier);
	}

	@Override
	public void deleteTokenMultiplier(Player player) {
		this.repository.deleteTokenMultiplier(player);
	}

	@Override
	public PlayerMultiplierBase getSellMultiplier(Player player) {
		return this.repository.getSellMultiplier(player);
	}

	@Override
	public PlayerMultiplierBase getTokenMultiplier(Player player) {
		return this.repository.getTokenMultiplier(player);
	}

	@Override
	public void removeExpiredMultipliers() {
		this.repository.removeExpiredMultipliers();
	}
}
