package dev.drawethree.xprison.autosell.api;

import dev.drawethree.xprison.api.autosell.XPrisonAutoSellAPI;
import dev.drawethree.xprison.api.autosell.model.SellRegion;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.SellRegionImpl;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public final class XPrisonAutoSellAPIImpl implements XPrisonAutoSellAPI {

	private final XPrisonAutoSell plugin;

	public XPrisonAutoSellAPIImpl(XPrisonAutoSell plugin) {
		this.plugin = plugin;
	}

	@Override
	public double getCurrentEarnings(Player player) {
		return plugin.getManager().getCurrentEarnings(player);
	}

	@Override
	public double getPriceForItem(SellRegion sellRegion, ItemStack itemStack) {
		return plugin.getManager().getPriceForItem(sellRegion, itemStack);
	}

	@Override
	public double getPriceForBlock(Block block) {
		return plugin.getManager().getPriceForBlock(block);
	}

	@Override
	public void sellBlocks(Player player, List<Block> blocks) {
		plugin.getManager().sellBlocks(player, blocks);
	}

	@Override
	public boolean hasAutoSellEnabled(Player p) {
		return plugin.getManager().hasAutoSellEnabled(p);
	}

	@Override
	public Collection<SellRegion> getSellRegions() {
		return plugin.getManager().getAutoSellRegions();
	}

	@Override
	public SellRegionImpl getSellRegionAtLocation(Location location) {
		return plugin.getManager().getAutoSellRegion(location);
	}
}
