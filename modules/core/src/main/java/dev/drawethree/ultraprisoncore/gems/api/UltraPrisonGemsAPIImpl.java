package dev.drawethree.ultraprisoncore.gems.api;

import dev.drawethree.ultraprisoncore.api.enums.LostCause;
import dev.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import dev.drawethree.ultraprisoncore.gems.managers.GemsManager;
import org.bukkit.OfflinePlayer;

public final class UltraPrisonGemsAPIImpl implements UltraPrisonGemsAPI {


	private final GemsManager manager;

	public UltraPrisonGemsAPIImpl(GemsManager manager) {
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
		this.manager.removeGems(p, amount, null, LostCause.ADMIN);
	}

	@Override
	public void addGems(OfflinePlayer p, long amount, ReceiveCause cause) {
		this.manager.giveGems(p, amount, null, cause);
	}
}
