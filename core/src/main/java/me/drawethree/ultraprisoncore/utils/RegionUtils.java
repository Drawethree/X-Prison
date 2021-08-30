package me.drawethree.ultraprisoncore.utils;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Set;
import java.util.stream.Collectors;

public class RegionUtils {

	public static IWrappedRegion getMineRegionWithHighestPriority(Location loc) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc).stream().filter(region -> region.getId().startsWith("mine")).collect(Collectors.toSet());
		IWrappedRegion lowestPrioRegion = null;
		for (IWrappedRegion region : regions) {
			if (lowestPrioRegion == null || region.getPriority() > lowestPrioRegion.getPriority()) {
				lowestPrioRegion = region;
			}
		}
		return lowestPrioRegion;
	}

	public static IWrappedRegion getFirstRegionAtLocation(Location loc) {
		Set<IWrappedRegion> regions = WorldGuardWrapper.getInstance().getRegions(loc);
		return regions.size() == 0 ? null : regions.iterator().next();
	}
}
