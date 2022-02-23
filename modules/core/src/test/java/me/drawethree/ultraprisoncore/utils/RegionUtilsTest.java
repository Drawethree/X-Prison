package me.drawethree.ultraprisoncore.utils;

import me.drawethree.ultraprisoncore.utils.misc.RegionUtils;
import org.bukkit.Location;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RegionUtils.class, WorldGuardWrapper.class})
public class RegionUtilsTest {

	private WorldGuardWrapper mockWrapper;
	private IWrappedRegion mockRegion1;
	private IWrappedRegion mockRegion2;
	private IWrappedRegion mockRegion3;

	private Location mockLocation;

	@Before
	public void setUp() {
		mockRegion1 = Mockito.mock(IWrappedRegion.class);
		mockRegion2 = Mockito.mock(IWrappedRegion.class);
		mockRegion3 = Mockito.mock(IWrappedRegion.class);
		mockLocation = Mockito.mock(Location.class);
		mockWrapper = Mockito.mock(WorldGuardWrapper.class);


		Mockito.when(mockRegion1.getId()).thenReturn("mine-a");
		Mockito.when(mockRegion2.getId()).thenReturn("mine-b");
		Mockito.when(mockRegion3.getId()).thenReturn("mine-c");

		Mockito.when(mockRegion1.getPriority()).thenReturn(10);
		Mockito.when(mockRegion2.getPriority()).thenReturn(20);
		Mockito.when(mockRegion3.getPriority()).thenReturn(30);

		PowerMockito.mockStatic(WorldGuardWrapper.class);
	}

	@Test
	public void test_success_getMineRegionWithHighestPriority_allRegionsAreMineRegions() {
		//Given
		Set<IWrappedRegion> regions = new HashSet<>();

		regions.add(mockRegion1);
		regions.add(mockRegion2);
		regions.add(mockRegion3);

		Mockito.when(WorldGuardWrapper.getInstance()).thenReturn(mockWrapper);
		Mockito.when(mockWrapper.getRegions(Mockito.any(Location.class))).thenReturn(regions);

		//When
		IWrappedRegion result = RegionUtils.getMineRegionWithHighestPriority(mockLocation);

		//Then
		Assert.assertEquals(mockRegion3, result);
	}

	@Test
	public void test_success_getMineRegionWithHighestPriority_noRegionsAtLocation() {
		//Given
		Set<IWrappedRegion> regions = new HashSet<>();

		Mockito.when(WorldGuardWrapper.getInstance()).thenReturn(mockWrapper);
		Mockito.when(mockWrapper.getRegions(Mockito.any(Location.class))).thenReturn(regions);

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

		Mockito.when(mockRegion1.getId()).thenReturn("12345");
		Mockito.when(mockRegion2.getId()).thenReturn("456");

		Mockito.when(WorldGuardWrapper.getInstance()).thenReturn(mockWrapper);
		Mockito.when(mockWrapper.getRegions(Mockito.any(Location.class))).thenReturn(regions);

		//When
		IWrappedRegion result = RegionUtils.getMineRegionWithHighestPriority(mockLocation);

		//Then
		Assert.assertEquals(mockRegion3, result);
	}

	@Test
	@Ignore
	public void getFirstRegionAtLocation() {
		//No need to write test for this...
	}
}