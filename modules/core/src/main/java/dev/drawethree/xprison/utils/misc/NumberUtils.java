package dev.drawethree.xprison.utils.misc;

import java.math.BigInteger;

public final class NumberUtils {

	private NumberUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}

	public static boolean isLongGreaterThanMaxLong(String input) {
		BigInteger bigIntegerInput = new BigInteger(input);
		return bigIntegerInput.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0;
	}

	public static boolean wouldAdditionBeOverMaxLong(long number1, long number2) {
		BigInteger bigInteger1 = new BigInteger(String.valueOf(number1));
		BigInteger bigInteger2 = new BigInteger(String.valueOf(number2));
		BigInteger result = bigInteger1.add(bigInteger2);

		return result.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0;
	}
}
