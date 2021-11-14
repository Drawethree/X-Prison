package me.drawethree.ultraprisoncore.utils;

import me.lucko.helper.text3.Text;
import org.bukkit.ChatColor;

public class ProgressBar {

	static final ChatColor AVAILABLE_COLOR = ChatColor.GREEN;
	static final ChatColor NOT_AVAILABLE_COLOR = ChatColor.RED;
	static final String DEFAULT_DELIMITER = ":";

	public static String getProgressBar(int amountOfDelimeters, String delimeter, double current, double required) {

		if (delimeter == null || delimeter.isEmpty()) {
			delimeter = DEFAULT_DELIMITER;
		}

		double treshold = required / amountOfDelimeters;

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < amountOfDelimeters; i++) {
			if (current >= treshold * (i + 1)) {
				result.append(AVAILABLE_COLOR).append(delimeter);
			} else {
				result.append(NOT_AVAILABLE_COLOR).append(delimeter);
			}
		}
		return Text.colorize(result.toString());
	}
}
