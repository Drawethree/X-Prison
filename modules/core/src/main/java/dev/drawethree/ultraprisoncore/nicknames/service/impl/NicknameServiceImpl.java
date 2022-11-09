package dev.drawethree.ultraprisoncore.nicknames.service.impl;

import dev.drawethree.ultraprisoncore.nicknames.repo.NicknameRepository;
import dev.drawethree.ultraprisoncore.nicknames.service.NicknameService;
import org.bukkit.OfflinePlayer;

public class NicknameServiceImpl implements NicknameService {

	private final NicknameRepository repository;

	public NicknameServiceImpl(NicknameRepository repository) {

		this.repository = repository;
	}

	@Override
	public void updatePlayerNickname(OfflinePlayer player) {
		repository.updatePlayerNickname(player);
	}
}
