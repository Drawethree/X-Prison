package me.drawethree.wildprisoncore.gems.api;

import me.drawethree.wildprisoncore.gems.managers.GemsManager;
import org.bukkit.OfflinePlayer;

public class WildPrisonGemsAPIImpl implements WildPrisonGemsAPI {


    private GemsManager manager;

    public WildPrisonGemsAPIImpl(GemsManager manager) {

        this.manager = manager;
    }

    @Override
	public long getPlayerGems(OfflinePlayer p) {
        return this.manager.getPlayerGems(p);
    }

    @Override
    public boolean hasEnough(OfflinePlayer p, long amount) {
		return this.getPlayerGems(p) >= amount;
    }

    @Override
	public void removeGems(OfflinePlayer p, long amount) {
        this.manager.removeGems(p, amount, null);
    }

    @Override
	public void addGems(OfflinePlayer p, long amount) {
        this.manager.giveGems(p, amount, null);
    }
}
