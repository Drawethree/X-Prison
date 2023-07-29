package dev.drawethree.xprison.utils.misc;

import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.ChatColor;

public class ProgressBar {

	static final ChatColor AVAILABLE_COLOR = ChatColor.GREEN;
	static final ChatColor NOT_AVAILABLE_COLOR = ChatColor.RED;
	static final String DEFAULT_DELIMITER = ":";

	public static String getProgressBar(int amountOfDelimeters, String delimeter, double current, double required) {

		if (delimeter == null || delimeter.isEmpty()) {
			delimeter = DEFAULT_DELIMITER;
		}

		if (current > required) {
			current = required;
		}

		double treshold = required / amountOfDelimeters;
		int numberOfGreens = (int) (current / treshold);

		StringBuilder result = new StringBuilder();

		result.append(AVAILABLE_COLOR);
		for (int i = 0; i < numberOfGreens; i++) {
			result.append(delimeter);
		}
		result.append(NOT_AVAILABLE_COLOR);
		for (int i = 0; i < amountOfDelimeters - numberOfGreens; i++) {
			result.append(delimeter);
		}
		return TextUtils.applyColor(result.toString());
	}

	private ProgressBar() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
