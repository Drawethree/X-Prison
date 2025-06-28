package dev.drawethree.xprison.autominer.api;

import dev.drawethree.xprison.api.autominer.XPrisonAutoMinerAPI;
import dev.drawethree.xprison.api.autominer.model.AutoMinerRegion;
import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import org.bukkit.entity.Player;

import java.util.Collection;

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

	@Override
	public Collection<AutoMinerRegion> getAutoMinerRegions() {
		return this.plugin.getManager().getAutoMinerRegions();
	}
}
