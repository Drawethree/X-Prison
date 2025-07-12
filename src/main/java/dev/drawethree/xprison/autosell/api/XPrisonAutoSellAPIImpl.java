package dev.drawethree.xprison.autosell.api;

import com.cryptomorin.xseries.XMaterial;
import dev.drawethree.xprison.api.autosell.XPrisonAutoSellAPI;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	public double getPriceForItem(ItemStack itemStack) {
		return plugin.getManager().getPriceForItem(itemStack);
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
	public void addSellPrice(XMaterial xMaterial, double v) {
		plugin.getManager().addSellPrice(xMaterial,v);
	}

	@Override
	public void removeSellPrice(XMaterial xMaterial) {
		plugin.getManager().removeSellPrice(xMaterial);
	}

	@Override
	public double getSellPriceForMaterial(XMaterial xMaterial) {
		return plugin.getManager().getSellPriceForMaterial(xMaterial);
	}
}
