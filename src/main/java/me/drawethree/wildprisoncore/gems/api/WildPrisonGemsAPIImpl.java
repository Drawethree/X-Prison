package me.drawethree.wildprisoncore.gems.api;

import me.drawethree.wildprisoncore.gems.managers.GemsManager;
import org.bukkit.OfflinePlayer;

public class WildPrisonGemsAPIImpl implements WildPrisonGemsAPI {


    private GemsManager manager;

    public WildPrisonGemsAPIImpl(GemsManager manager) {

        this.manager = manager;
    }

    @Override
    public long getPlayerTokens(OfflinePlayer p) {
        return this.manager.getPlayerGems(p);
    }

    @Override
    public boolean hasEnough(OfflinePlayer p, long amount) {
        return this.getPlayerTokens(p) > amount;
    }

    @Override
    public void removeTokens(OfflinePlayer p, long amount) {
        this.manager.removeGems(p, amount, null);
    }

    @Override
    public void addTokens(OfflinePlayer p, long amount) {
        this.manager.giveGems(p, amount, null);
    }
}
