package me.drawethree.ultraprisoncore.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerUtils {

	public static void sendMessage(Player player, String message) {
		if (StringUtils.isBlank(message)) {
			return;
		}
		player.sendMessage(message);
	}

	public static void sendMessage(Player player, List<String> message) {
		for (String s : message) {
			if (StringUtils.isBlank(s)) {
				return;
			}
			player.sendMessage(s);
		}
	}
}
