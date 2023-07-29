package dev.drawethree.xprison.utils.text;

import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

	private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

	public static String applyColor(String message) {
		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_16)) {
			Matcher matcher = HEX_PATTERN.matcher(message);
			while (matcher.find()) {
				final ChatColor hexColor = ChatColor.of(matcher.group().substring(1));
				final String before = message.substring(0, matcher.start());
				final String after = message.substring(matcher.end());
				message = before + hexColor + after;
				matcher = HEX_PATTERN.matcher(message);
			}
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static List<String> applyColor(List<String> list) {
		List<String> returnVal = new ArrayList<>(list.size());
		list.forEach(s -> returnVal.add(applyColor(s)));
		return returnVal;
	}

	private TextUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
