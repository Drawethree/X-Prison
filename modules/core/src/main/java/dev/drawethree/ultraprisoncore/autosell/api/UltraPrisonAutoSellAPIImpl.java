package dev.drawethree.ultraprisoncore.autosell.api;

import dev.drawethree.ultraprisoncore.autosell.SellRegion;
import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

public class UltraPrisonAutoSellAPIImpl implements UltraPrisonAutoSellAPI {

	private UltraPrisonAutoSell plugin;

	public UltraPrisonAutoSellAPIImpl(UltraPrisonAutoSell plugin) {
		this.plugin = plugin;
	}

	@Override
	public double getCurrentEarnings(Player player) {
		return plugin.getCurrentEarnings(player);
	}

	@Override
	public double getPriceForBlock(String regionName, Block block) {
		return plugin.getPriceForBrokenBlock(regionName, block);
	}

	@Override
	public boolean hasAutoSellEnabled(Player p) {
		return plugin.hasAutoSellEnabled(p);
	}

	@Override
	public Collection<SellRegion> getSellRegions() {
		return plugin.getAutoSellRegions();
	}

	@Override
	public SellRegion getSellRegionAtLocation(Location location) {
		return plugin.getAutoSellRegion(location);
	}
}
