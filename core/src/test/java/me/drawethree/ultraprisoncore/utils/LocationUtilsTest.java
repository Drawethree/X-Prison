package me.drawethree.ultraprisoncore.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LocationUtilsTest {


	private Location mockLocation;
	private World mockWorld;

	@Before
	public void setUp() {

	}

	@Test
	public void test_success_toXYZW_0_0_0_world() {

		this.mockWorld = Mockito.mock(World.class);
		this.mockLocation = Mockito.mock(Location.class);

		String expected = "X: 0, Y: 0, Z: 0, World: world";

		Mockito.when(mockWorld.getName()).thenReturn("world");
		Mockito.when(mockLocation.getWorld()).thenReturn(mockWorld);
		Mockito.when(mockLocation.getBlockX()).thenReturn(0);
		Mockito.when(mockLocation.getBlockY()).thenReturn(0);
		Mockito.when(mockLocation.getBlockZ()).thenReturn(0);

		String actual = LocationUtils.toXYZW(mockLocation);

		Assert.assertEquals(expected, actual);

	}

	@Test
	public void test_sucecss_toXYZW_null() {

		this.mockLocation = null;

		String expected = LocationUtils.INVALID_LOCATION;

		String actual = LocationUtils.toXYZW(mockLocation);

		Assert.assertEquals(expected, actual);

	}
}