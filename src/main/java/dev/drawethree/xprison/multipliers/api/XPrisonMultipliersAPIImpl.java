package dev.drawethree.xprison.multipliers.api;

import dev.drawethree.xprison.api.multipliers.XPrisonMultipliersAPI;
import dev.drawethree.xprison.api.multipliers.model.Multiplier;
import dev.drawethree.xprison.api.multipliers.model.MultiplierType;
import dev.drawethree.xprison.api.multipliers.model.PlayerMultiplier;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import org.bukkit.entity.Player;

public final class XPrisonMultipliersAPIImpl implements XPrisonMultipliersAPI {

	private final XPrisonMultipliers plugin;

	public XPrisonMultipliersAPIImpl(XPrisonMultipliers plugin) {

		this.plugin = plugin;
	}

	@Override
	public Multiplier getGlobalSellMultiplier() {
		return plugin.getGlobalSellMultiplier();
	}

	@Override
	public Multiplier getGlobalTokenMultiplier() {
		return plugin.getGlobalTokenMultiplier();
	}

	@Override
	public PlayerMultiplier getSellMultiplier(Player p) {
		return plugin.getSellMultiplier(p);
	}

	@Override
	public PlayerMultiplier getTokenMultiplier(Player p) {
		return plugin.getTokenMultiplier(p);
	}

	@Override
	public PlayerMultiplier getRankMultiplier(Player p) {
		return plugin.getRankMultiplier(p);
	}

	@Override
	public double getPlayerMultiplier(Player p, MultiplierType type) {
		double toReturn = 0.0;

		switch (type) {
			case SELL:
				PlayerMultiplier sellMulti = this.getSellMultiplier(p);
				if (sellMulti != null && sellMulti.isActive()) {
					toReturn += sellMulti.getMultiplier();
				}
				if (this.getGlobalSellMultiplier().isActive()) {
					toReturn += this.getGlobalSellMultiplier().getMultiplier();
				}
				break;
			case TOKENS:
				PlayerMultiplier tokenMulti = this.getTokenMultiplier(p);
				if (tokenMulti != null && tokenMulti.isActive()) {
					toReturn += tokenMulti.getMultiplier();
				}
				if (this.getGlobalTokenMultiplier().isActive()) {
					toReturn += this.getGlobalSellMultiplier().getMultiplier();
				}
				break;
		}
		PlayerMultiplier rankMulti = this.getRankMultiplier(p);
		toReturn += rankMulti == null ? 0.0 : rankMulti.getMultiplier();
		return toReturn;
	}

}
