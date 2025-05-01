package dev.drawethree.xprison.utils.misc;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class RegionUtils {

	public static IWrappedRegion getRegionWithHighestPriority(Location loc) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc);
		IWrappedRegion highestPrioRegion = null;
		for (IWrappedRegion region : regions) {
			if (highestPrioRegion == null || region.getPriority() > highestPrioRegion.getPriority()) {
				highestPrioRegion = region;
			}
		}
		return highestPrioRegion;
	}

	public static IWrappedRegion getRegionWithHighestPriorityAndFlag(Location loc, String flagName, Object flagValue) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc);

		IWrappedRegion highestPrioRegion = null;

		for (IWrappedRegion region : regions) {
			for (Map.Entry<IWrappedFlag<?>, Object> flag : region.getFlags().entrySet()) {
				if (flag.getKey().getName().equalsIgnoreCase(flagName) && flag.getValue().equals(flagValue)) {
					if (highestPrioRegion == null || region.getPriority() > highestPrioRegion.getPriority()) {
						highestPrioRegion = region;
					}
				}
			}
		}
		return highestPrioRegion;
	}

	public static IWrappedRegion getFirstRegionAtLocation(Location loc) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc);
		return regions.isEmpty() ? null : regions.iterator().next();
	}

	@Nullable
	public static String getMineRomanName(Location loc) {
		IWrappedRegion region = getFirstRegionAtLocation(loc);
		if (region != null && region.getId().startsWith("mina")) {
			try {
				int number = Integer.parseInt(region.getId().substring(4));
				return toRoman(number);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	@NotNull
	@Contract(pure = true)
	private static String toRoman(int number) {
		if (number < 1 || number > 100) return String.valueOf(number);

		String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
		String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
		String[] hundreds = {"", "C"};

		return hundreds[number / 100] + tens[(number % 100) / 10] + units[number % 10];
	}

	private RegionUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
