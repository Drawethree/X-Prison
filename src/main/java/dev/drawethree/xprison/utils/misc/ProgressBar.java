package dev.drawethree.xprison.utils.misc;

import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class ProgressBar {

	static final ChatColor AVAILABLE_COLOR = ChatColor.GREEN;
	static final ChatColor NOT_AVAILABLE_COLOR = ChatColor.RED;
	static final String DEFAULT_DELIMITER = ":";

	@NotNull
	public static String getProgressBar(int amountOfDelimiters, String delimeter, double current, double required) {
		if (delimeter == null || delimeter.isEmpty()) {
			delimeter = DEFAULT_DELIMITER;
		}

		if (current > required) {
			current = required;
		}

		double treshold = required / amountOfDelimiters;
		int numberOfGreens = (int) (current / treshold);

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < amountOfDelimiters; i++) {
			if (i < numberOfGreens) {
				result.append(AVAILABLE_COLOR).append(delimeter);
			} else {
				result.append(NOT_AVAILABLE_COLOR).append(delimeter);
			}
		}

		return TextUtils.applyColor(result.toString());
	}

	private ProgressBar() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
