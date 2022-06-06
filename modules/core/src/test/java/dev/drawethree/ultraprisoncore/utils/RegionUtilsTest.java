package dev.drawethree.ultraprisoncore.utils;

import dev.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class RegionUtilsTest {

	@Mock
	private WorldGuardWrapper mockWrapper;
	@Mock
	private IWrappedRegion mockRegion1;
	@Mock
	private IWrappedRegion mockRegion2;
	@Mock
	private IWrappedRegion mockRegion3;
	@Mock
	private Location mockLocation;

	@BeforeClass
	public static void beforeClass() throws Exception {
		mockStatic(WorldGuardWrapper.class);
	}

	@Before
	public void setUp() {
		mockRegion1 = mock(IWrappedRegion.class);
		mockRegion2 = mock(IWrappedRegion.class);
		mockRegion3 = mock(IWrappedRegion.class);
		mockLocation = mock(Location.class);
		mockWrapper = mock(WorldGuardWrapper.class);

		when(mockRegion1.getId()).thenReturn("mine-a");
		when(mockRegion2.getId()).thenReturn("mine-b");
		when(mockRegion3.getId()).thenReturn("mine-c");

		when(mockRegion1.getPriority()).thenReturn(10);
		when(mockRegion2.getPriority()).thenReturn(20);
		when(mockRegion3.getPriority()).thenReturn(30);
	}

	@Test
	public void test_success_getMineRegionWithHighestPriority_allRegionsAreMineRegions() {
		//Given
		Set<IWrappedRegion> regions = new HashSet<>();

		regions.add(mockRegion1);
		regions.add(mockRegion2);
		regions.add(mockRegion3);

		when(WorldGuardWrapper.getInstance()).thenReturn(mockWrapper);
		when(mockWrapper.getRegions(any(Location.class))).thenReturn(regions);

		//When
		IWrappedRegion result = RegionUtils.getMineRegionWithHighestPriority(mockLocation);

		//Then
		Assert.assertEquals(mockRegion3, result);
	}

	@Test
	public void test_success_getMineRegionWithHighestPriority_noRegionsAtLocation() {
		//Given
		Set<IWrappedRegion> regions = new HashSet<>();

		when(WorldGuardWrapper.getInstance()).thenReturn(mockWrapper);
		when(mockWrapper.getRegions(any(Location.class))).thenReturn(regions);

		//When
		IWrappedRegion result = RegionUtils.getMineRegionWithHighestPriority(mockLocation);

		//Then
		Assert.assertNull(result);
	}

	@Test
	public void test_success_getMineRegionWithHighestPriority_onlyOneMineRegion() {
		//Given
		Set<IWrappedRegion> regions = new HashSet<>();

		regions.add(mockRegion1);
		regions.add(mockRegion2);
		regions.add(mockRegion3);

		when(mockRegion1.getId()).thenReturn("12345");
		when(mockRegion2.getId()).thenReturn("456");

		when(WorldGuardWrapper.getInstance()).thenReturn(mockWrapper);
		when(mockWrapper.getRegions(any(Location.class))).thenReturn(regions);

		//When
		IWrappedRegion result = RegionUtils.getMineRegionWithHighestPriority(mockLocation);

		//Then
		Assert.assertEquals(mockRegion3, result);
	}
}