package dev.drawethree.xprison.autominer.utils;

public class AutoMinerUtils {

	public static String getAutoMinerTimeLeftFormatted(int timeLeft) {

		if (timeLeft == 0) {
			return "0s";
		}

		long days = timeLeft / (24 * 60 * 60);
		timeLeft -= days * (24 * 60 * 60);

		long hours = timeLeft / (60 * 60);
		timeLeft -= hours * (60 * 60);

		long minutes = timeLeft / (60);
		timeLeft -= minutes * (60);

		long seconds = timeLeft;

		return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
	}
}
