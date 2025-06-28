package dev.drawethree.xprison.tokens.api;

import dev.drawethree.xprison.api.shared.currency.enums.LostCause;
import dev.drawethree.xprison.api.shared.currency.enums.ReceiveCause;
import dev.drawethree.xprison.api.tokens.XPrisonTokensAPI;
import dev.drawethree.xprison.tokens.managers.TokensManager;
import org.bukkit.OfflinePlayer;

public final class XPrisonTokensAPIImpl implements XPrisonTokensAPI {

	private final TokensManager manager;

	public XPrisonTokensAPIImpl(TokensManager manager) {
		this.manager = manager;
	}

	@Override
	public long getAmount(OfflinePlayer p) {
		return this.manager.getPlayerTokens(p);
	}

	@Override
	public boolean hasEnough(OfflinePlayer p, long amount) {
		return this.getAmount(p) >= amount;
	}

	@Override
	public void remove(OfflinePlayer offlinePlayer, long amount, LostCause cause) {
		this.manager.removeTokens(offlinePlayer, amount, null, cause);
	}

	@Override
	public void add(OfflinePlayer offlinePlayer, long amount, ReceiveCause cause) {
		this.manager.giveTokens(offlinePlayer, amount, null, cause);
	}

	@Override
	public void set(OfflinePlayer offlinePlayer, long l) {
		this.manager.setTokens(offlinePlayer,l,null);
	}
}
