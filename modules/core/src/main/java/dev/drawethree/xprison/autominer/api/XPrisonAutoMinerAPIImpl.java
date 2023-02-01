package dev.drawethree.xprison.autominer.api;

import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import org.bukkit.entity.Player;

public final class XPrisonAutoMinerAPIImpl implements XPrisonAutoMinerAPI {

	private final XPrisonAutoMiner plugin;

	public XPrisonAutoMinerAPIImpl(XPrisonAutoMiner plugin) {
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
