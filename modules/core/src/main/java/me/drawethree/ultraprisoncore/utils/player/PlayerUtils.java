package me.drawethree.ultraprisoncore.utils.player;

import me.drawethree.ultraprisoncore.utils.text.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerUtils {

	/**
	 * Sends a message to a player and replaces colors and also hex colors
	 *
	 * @param commandSender to who should be message send
	 * @param message message
	 */
	public static void sendMessage(CommandSender commandSender, String message) {
		if (commandSender instanceof Player && !((Player) commandSender).isOnline()) {
			return;
		}
		if (StringUtils.isBlank(message)) {
			return;
		}
		commandSender.sendMessage(TextUtils.applyColor(message));
	}

	/**
	 * Sends multiple message to a player and replaces colors and also hex colors
	 *
	 * @param commandSender to who should be message send
	 * @param message message
	 */
	public static void sendMessage(CommandSender commandSender, List<String> message) {
		if (commandSender instanceof Player && !((Player) commandSender).isOnline()) {
			return;
		}
		for (String s : message) {
			if (StringUtils.isBlank(s)) {
				return;
			}
			commandSender.sendMessage(TextUtils.applyColor(s));
		}
	}

	private PlayerUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
