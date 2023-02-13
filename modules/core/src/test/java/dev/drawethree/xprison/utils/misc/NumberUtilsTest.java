package dev.drawethree.xprison.utils.misc;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NumberUtilsTest {

	@Test
	public void test_lower1() {
		String input = "1000000";
		boolean result = NumberUtils.isLongGreaterThanMaxLong(input);
		assertFalse(result);
	}

	@Test
	public void test_lower2() {
		long number1 = Long.MAX_VALUE - 100;
		long number2 = 10;
		boolean result = NumberUtils.wouldAdditionBeOverMaxLong(number1, number2);
		assertFalse(result);
	}

	@Test
	public void test_greater() {
		String input = Long.MAX_VALUE + "1";
		boolean result = NumberUtils.isLongGreaterThanMaxLong(input);
		assertTrue(result);
	}

	@Test
	public void test_greater2() {
		long number1 = Long.MAX_VALUE;
		long number2 = 10;
		boolean result = NumberUtils.wouldAdditionBeOverMaxLong(number1, number2);
		assertTrue(result);
	}
}