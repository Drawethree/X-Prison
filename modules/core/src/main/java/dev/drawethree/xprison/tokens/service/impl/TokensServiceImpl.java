package dev.drawethree.xprison.tokens.service.impl;

import dev.drawethree.xprison.tokens.repo.TokensRepository;
import dev.drawethree.xprison.tokens.service.TokensService;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public class TokensServiceImpl implements TokensService {

	private final TokensRepository repository;
	private TokensRepository tokensRepository;

	public TokensServiceImpl(TokensRepository repository) {

		this.repository = repository;
	}

	@Override
	public long getTokens(OfflinePlayer player) {
		return repository.getPlayerTokens(player);
	}

	@Override
	public void setTokens(OfflinePlayer player, long newAmount) {
		repository.updateTokens(player, newAmount);
	}

	@Override
	public Map<UUID, Long> getTopTokens(int amountOfRecords) {
		return repository.getTopTokens(amountOfRecords);
	}

	@Override
	public void createTokens(OfflinePlayer player, long startingTokens) {
		repository.addIntoTokens(player, startingTokens);
	}
}
