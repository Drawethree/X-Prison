package me.drawethree.ultraprisoncore.utils.misc;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Set;
import java.util.stream.Collectors;

public class RegionUtils {

	public static IWrappedRegion getMineRegionWithHighestPriority(Location loc) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc).stream().filter(region -> region.getId().startsWith("mine")).collect(Collectors.toSet());
		IWrappedRegion highestPrioRegion = null;
		for (IWrappedRegion region : regions) {
			if (highestPrioRegion == null || region.getPriority() > highestPrioRegion.getPriority()) {
				highestPrioRegion = region;
			}
		}
		return highestPrioRegion;
	}

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

	public static IWrappedRegion getFirstRegionAtLocation(Location loc) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc);
		return regions.size() == 0 ? null : regions.iterator().next();
	}

	private RegionUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
