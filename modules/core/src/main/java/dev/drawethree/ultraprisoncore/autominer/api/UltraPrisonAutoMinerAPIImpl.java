package dev.drawethree.ultraprisoncore.autominer.api;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import org.bukkit.entity.Player;

public final class UltraPrisonAutoMinerAPIImpl implements UltraPrisonAutoMinerAPI {

	private final UltraPrisonAutoMiner plugin;

	public UltraPrisonAutoMinerAPIImpl(UltraPrisonAutoMiner plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean isInAutoMinerRegion(Player player) {
		return this.plugin.getManager().isInAutoMinerRegion(player);
	}

	@Override
	public int getAutoMinerTime(Player player) {
		return this.plugin.getManager().getAutoMinerTime(player);
	}
}
