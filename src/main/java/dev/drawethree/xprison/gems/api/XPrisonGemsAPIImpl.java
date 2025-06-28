package dev.drawethree.xprison.gems.api;

import dev.drawethree.xprison.api.gems.XPrisonGemsAPI;
import dev.drawethree.xprison.api.shared.currency.enums.LostCause;
import dev.drawethree.xprison.api.shared.currency.enums.ReceiveCause;
import dev.drawethree.xprison.gems.managers.GemsManager;
import org.bukkit.OfflinePlayer;

public final class XPrisonGemsAPIImpl implements XPrisonGemsAPI {


    private final GemsManager manager;

    public XPrisonGemsAPIImpl(GemsManager manager) {
        this.manager = manager;
    }

    @Override
    public long getAmount(OfflinePlayer p) {
        return this.manager.getPlayerGems(p);
    }

    @Override
    public boolean hasEnough(OfflinePlayer p, long amount) {
        return this.getAmount(p) >= amount;
    }

    @Override
    public void remove(OfflinePlayer p, long amount, LostCause lostCause) {
        this.manager.removeGems(p, amount, null, lostCause);
    }

    @Override
    public void add(OfflinePlayer p, long amount, ReceiveCause cause) {
        this.manager.giveGems(p, amount, null, cause);
    }

    @Override
    public void set(OfflinePlayer offlinePlayer, long l) {
        this.manager.setGems(offlinePlayer, l, null);
    }
}
