package dev.drawethree.xprison.utils.player;

import dev.drawethree.xprison.utils.text.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class PlayerUtils {

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

	/**
	 * Sends a title with subtitle to a player and replaces colors and also hex colors
	 *
	 * @param player to who should be message send
	 * @param title title
	 * @param subTitle sub title
	 */
	public static void sendTitle(Player player, String title, String subTitle) {
		if (!player.isOnline()) {
			return;
		}

		if (StringUtils.isBlank(title) || StringUtils.isBlank(subTitle)) {
			return;
		}

		player.sendTitle(TextUtils.applyColor(title),TextUtils.applyColor(subTitle));
	}

	private PlayerUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
