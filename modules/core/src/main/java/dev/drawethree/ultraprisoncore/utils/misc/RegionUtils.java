package dev.drawethree.ultraprisoncore.utils.misc;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Optional;
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

	public static IWrappedRegion getRegionWithHighestPriorityAndFlag(Location loc, IWrappedFlag<WrappedState> flag) {
		Optional<WrappedState> optional = WorldGuardWrapper.getInstance().queryFlag(null, loc, flag);

		if (!optional.isPresent() || optional.get() == WrappedState.DENY) {
			return null;
		}

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
