package dev.drawethree.xprison.multipliers.api;

import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import dev.drawethree.xprison.multipliers.multiplier.GlobalMultiplier;
import dev.drawethree.xprison.multipliers.multiplier.Multiplier;
import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.entity.Player;

public final class XPrisonMultipliersAPIImpl implements XPrisonMultipliersAPI {

	private final XPrisonMultipliers plugin;

	public XPrisonMultipliersAPIImpl(XPrisonMultipliers plugin) {

		this.plugin = plugin;
	}

	@Override
	public GlobalMultiplier getGlobalSellMultiplier() {
		return plugin.getGlobalSellMultiplier();
	}

	@Override
	public GlobalMultiplier getGlobalTokenMultiplier() {
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
	public Multiplier getRankMultiplier(Player p) {
		return plugin.getRankMultiplier(p);
	}

	@Override
	public double getPlayerMultiplier(Player p, MultiplierType type) {
		double toReturn = 0.0;

		switch (type) {
			case SELL:
				PlayerMultiplier sellMulti = this.getSellMultiplier(p);
				if (sellMulti != null && !sellMulti.isExpired()) {
					toReturn += sellMulti.getMultiplier();
				}
				if (!this.getGlobalSellMultiplier().isExpired()) {
					toReturn += this.getGlobalSellMultiplier().getMultiplier();
				}
				break;
			case TOKENS:
				PlayerMultiplier tokenMulti = this.getTokenMultiplier(p);
				if (tokenMulti != null && !tokenMulti.isExpired()) {
					toReturn += tokenMulti.getMultiplier();
				}
				if (!this.getGlobalTokenMultiplier().isExpired()) {
					toReturn += this.getGlobalSellMultiplier().getMultiplier();
				}
				break;
		}
		Multiplier rankMulti = this.getRankMultiplier(p);
		toReturn += rankMulti == null ? 0.0 : rankMulti.getMultiplier();
		return toReturn;
	}

}
