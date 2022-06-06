package dev.drawethree.ultraprisoncore.utils.misc;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.drawethree.ultraprisoncore.utils.misc.MathUtils.formatNumber;
import static org.junit.Assert.assertEquals;

public class MathUtilsTest {

	private Map<Double, String> numbersToTest;

	@Before
	public void before() {
		this.numbersToTest = new HashMap<>();

		this.numbersToTest.put(500.0, "500.0");
		this.numbersToTest.put(1000.0, "1000.0");
		this.numbersToTest.put(5000.0, "5.0k");
		this.numbersToTest.put(5500.0, "5.5k");

		this.numbersToTest.put(-500.0, "-500.0");
		this.numbersToTest.put(-1000.0, "-1000.0");
		this.numbersToTest.put(-5000.0, "-5.0k");
		this.numbersToTest.put(-5500.0, "-5.5k");
	}

	@Test
	public void formatNumbers() {
		for (Map.Entry<Double, String> entry : this.numbersToTest.entrySet()) {
			assertEquals(entry.getValue(), formatNumber(entry.getKey()));
		}
	}
}