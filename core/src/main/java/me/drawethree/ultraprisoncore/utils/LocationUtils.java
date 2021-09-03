package me.drawethree.ultraprisoncore.utils;

import org.bukkit.Location;

public class LocationUtils {

	public static final String INVALID_LOCATION = "Invalid Location";

	public static String toXYZW(Location location) {
		if (location == null) {
			return INVALID_LOCATION;
		}
		return "X: " +
				location.getBlockX() +
				", " +
				"Y: " +
				location.getBlockY() +
				", " +
				"Z: " +
				location.getBlockZ() +
				", " +
				"World: " +
				location.getWorld().getName();
	}
}
