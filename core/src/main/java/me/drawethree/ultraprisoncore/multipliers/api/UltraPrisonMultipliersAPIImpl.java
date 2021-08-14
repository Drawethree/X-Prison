package me.drawethree.ultraprisoncore.multipliers.api;

import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import me.drawethree.ultraprisoncore.multipliers.multiplier.GlobalMultiplier;
import me.drawethree.ultraprisoncore.multipliers.multiplier.Multiplier;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import org.bukkit.entity.Player;

public class UltraPrisonMultipliersAPIImpl implements UltraPrisonMultipliersAPI {

	private UltraPrisonMultipliers plugin;

	public UltraPrisonMultipliersAPIImpl(UltraPrisonMultipliers plugin) {

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
		double toReturn = 1.0;

		PlayerMultiplier sellMulti = this.getSellMultiplier(p);
		PlayerMultiplier tokenMulti = this.getTokenMultiplier(p);
		Multiplier rankMulti = this.getRankMultiplier(p);

		switch (type) {
			case SELL:
				toReturn += this.getGlobalSellMultiplier().getMultiplier();
				toReturn += sellMulti == null ? 0.0 : sellMulti.getMultiplier();
				break;
			case TOKENS:
				toReturn += this.getGlobalTokenMultiplier().getMultiplier();
				toReturn += tokenMulti == null ? 0.0 : tokenMulti.getMultiplier();
				break;
		}
		toReturn += rankMulti == null ? 0.0 : rankMulti.getMultiplier();
		return toReturn;
	}

}
