package dev.drawethree.xprison.utils.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MathUtils {

	/**
	 * Formats number to format ('k','M','B','T','q','Q','QT','S','SP','O','N','D'
	 * If number is or equal to 1000.0 will return the original number.
	 * Supports also negative amounts
	 *
	 * @param amount number to format
	 * @return Formatted number as string
	 */
	public static String formatNumber(double amount) {

		boolean negative = amount < 0;
		String prefix = "";

		if (negative) {
			amount = Math.abs(amount);
			prefix = "-";
		}

		if (amount <= 1000.0D)
			return prefix + amount;
		List<String> suffixes = new ArrayList<>(Arrays.asList("", "k", "M", "B", "T", "q", "Q", "QT", "S", "SP", "O",
				"N", "D"));
		double chunks = Math.floor(Math.floor(Math.log10(amount) / 3.0D));
		amount /= Math.pow(10.0D, chunks * 3.0D - 1.0D);
		amount /= 10.0D;
		String suffix = suffixes.get((int) chunks);
		String format = String.valueOf(amount);
		if (format.replace(".", "").length() > 5)
			format = format.substring(0, 5);
		return prefix + format + suffix;
	}

	public static String formatNumberNoDecimals(double amount) {
		boolean negative = amount < 0;
		String prefix = negative ? "-" : "";

		if (negative) amount = Math.abs(amount);
		if (amount < 1000) return prefix + (int) amount;

		List<String> suffixes = Arrays.asList("", "K", "M", "B", "T", "q", "Q", "QT", "S", "SP", "O", "N", "D");
		int index = (int) (Math.log10(amount) / 3);
		double scaled = amount / Math.pow(1000, index);

		String formatted = String.format("%.0f", scaled);
		return prefix + formatted + suffixes.get(index);
	}

	private MathUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}

}
