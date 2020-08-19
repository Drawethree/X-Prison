package me.drawethree.wildprisoncore.tokens.api;

import me.drawethree.wildprisoncore.tokens.managers.TokensManager;
import org.bukkit.OfflinePlayer;

public class WildPrisonTokensAPIImpl implements WildPrisonTokensAPI {


    private TokensManager manager;

    public WildPrisonTokensAPIImpl(TokensManager manager) {

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
    public void removeTokens(OfflinePlayer p, long amount) {
        this.manager.removeTokens(p, amount, null);
    }

    @Override
    public void addTokens(OfflinePlayer p, long amount) {
        this.manager.giveTokens(p, amount, null);
    }
}
