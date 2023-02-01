package dev.drawethree.xprison.tokens.service.impl;

import dev.drawethree.xprison.tokens.repo.BlocksRepository;
import dev.drawethree.xprison.tokens.service.BlocksService;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public class BlocksServiceImpl implements BlocksService {

	private final BlocksRepository repository;

	public BlocksServiceImpl(BlocksRepository repository) {
		this.repository = repository;
	}

	@Override
	public void resetBlocksWeekly() {
		repository.resetBlocksWeekly();
	}

	@Override
	public void setBlocks(OfflinePlayer player, long newAmount) {
		repository.updateBlocks(player, newAmount);
	}

	@Override
	public void setBlocksWeekly(OfflinePlayer player, long newAmount) {
		repository.updateBlocksWeekly(player, newAmount);
	}

	@Override
	public long getPlayerBrokenBlocksWeekly(OfflinePlayer player) {
		return repository.getPlayerBrokenBlocksWeekly(player);
	}

	@Override
	public void createBlocks(OfflinePlayer player) {
		repository.addIntoBlocks(player);
	}

	@Override
	public void createBlocksWeekly(OfflinePlayer player) {
		repository.addIntoBlocksWeekly(player);
	}

	@Override
	public long getPlayerBrokenBlocks(OfflinePlayer player) {
		return repository.getPlayerBrokenBlocks(player);
	}

	@Override
	public Map<UUID, Long> getTopBlocksWeekly(int amountOfRecords) {
		return repository.getTopBlocksWeekly(amountOfRecords);
	}

	@Override
	public Map<UUID, Long> getTopBlocks(int amountOfRecords) {
		return repository.getTopBlocks(amountOfRecords);
	}
}
