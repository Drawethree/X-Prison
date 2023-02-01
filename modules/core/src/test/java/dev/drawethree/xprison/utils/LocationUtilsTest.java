package dev.drawethree.xprison.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static dev.drawethree.xprison.utils.location.LocationUtils.INVALID_LOCATION;
import static dev.drawethree.xprison.utils.location.LocationUtils.toXYZW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LocationUtilsTest {


	@Mock
	private Location mockLocation;

	@Mock
	private World mockWorld;

	@BeforeEach
	public void setUp() {

	}

	@Test
	public void test_success_toXYZW_0_0_0_world() {

		this.mockWorld = mock(World.class);
		this.mockLocation = mock(Location.class);

		String expected = "X: 0, Y: 0, Z: 0, World: world";

		when(mockWorld.getName()).thenReturn("world");
		when(mockLocation.getWorld()).thenReturn(mockWorld);
		when(mockLocation.getBlockX()).thenReturn(0);
		when(mockLocation.getBlockY()).thenReturn(0);
		when(mockLocation.getBlockZ()).thenReturn(0);

		String actual = toXYZW(mockLocation);

		assertEquals(expected, actual);

	}

	@Test
	public void test_sucecss_toXYZW_null() {

		this.mockLocation = null;

		String actual = toXYZW(mockLocation);

		assertEquals(INVALID_LOCATION, actual);

	}
}