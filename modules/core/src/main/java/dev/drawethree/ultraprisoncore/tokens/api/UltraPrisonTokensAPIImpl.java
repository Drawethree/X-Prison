package dev.drawethree.ultraprisoncore.tokens.api;

import dev.drawethree.ultraprisoncore.api.enums.LostCause;
import dev.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import dev.drawethree.ultraprisoncore.tokens.managers.TokensManager;
import org.bukkit.OfflinePlayer;

public final class UltraPrisonTokensAPIImpl implements UltraPrisonTokensAPI {

	private final TokensManager manager;

	public UltraPrisonTokensAPIImpl(TokensManager manager) {
		this.manager = manager;
	}

	@Override
	public long getPlayerTokens(OfflinePlayer p) {
		return this.manager.getPlayerTokens(p);
	}

	@Override
	public boolean hasEnough(OfflinePlayer p, long amount) {
		return this.getPlayerTokens(p) >= amount;
	}

	@Override
	public void removeTokens(OfflinePlayer p, long amount, LostCause cause) {
		this.manager.removeTokens(p, amount, null, cause);
	}

	@Override
	public void addTokens(OfflinePlayer p, long amount, ReceiveCause cause) {
		this.manager.giveTokens(p, amount, null, cause);
	}
}
