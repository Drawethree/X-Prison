package dev.drawethree.ultraprisoncore.autosell.api;

import dev.drawethree.ultraprisoncore.autosell.model.SellRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface UltraPrisonAutoSellAPI {

	/**
	 * Method to get current earnings of player
	 *
	 * @param player Player
	 * @return Current earnings
	 */
	double getCurrentEarnings(Player player);

	/**
	 * Method to get price for ItemStack in specified mine region
	 *
	 * @param regionName Name of region
	 * @param item       ItemStack
	 * @return Price for item
	 */
	double getPriceForItem(String regionName, ItemStack item);

	/**
	 * Method to get if player has autosell enabled
	 *
	 * @param p Player
	 * @return true if player has autosell enabled, otherwise false
	 */
	boolean hasAutoSellEnabled(Player p);

	/**
	 * Method to get all sell regions
	 *
	 * @return Collection of all loaded and active Sell Regions
	 */
	Collection<SellRegion> getSellRegions();

	/**
	 * Method to get SellRegion at specified location
	 *
	 * @return Sell Region at given location or null if not present.
	 */
	SellRegion getSellRegionAtLocation(Location location);
}
