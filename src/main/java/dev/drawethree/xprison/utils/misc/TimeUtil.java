package dev.drawethree.xprison.utils.misc;

import java.util.StringJoiner;

public class TimeUtil {

	public static String getTime(long seconds) {
		final StringJoiner joiner = new StringJoiner(" ");

		long minutes = seconds / 60;
		long hours = minutes / 60;
		final long days = hours / 24;

		seconds %= 60;
		minutes %= 60;
		hours %= 24;

		if (days > 0) {
			joiner.add(days + "d");
		}

		if (hours > 0) {
			joiner.add(hours + "h");
		}

		if (minutes > 0) {
			joiner.add(minutes + "m");
		}

		if (seconds > 0) {
			joiner.add(seconds + "s");
		}

		return joiner.toString();
	}

	private TimeUtil() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
