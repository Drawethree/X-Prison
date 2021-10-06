package me.drawethree.ultraprisoncore.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerUtils {

	public static void sendMessage(CommandSender commandSender, String message) {
		if (commandSender instanceof Player && !((Player) commandSender).isOnline()) {
			return;
		}
		if (StringUtils.isBlank(message)) {
			return;
		}
		commandSender.sendMessage(message);
	}

	public static void sendMessage(CommandSender commandSender, List<String> message) {
		if (commandSender instanceof Player && !((Player) commandSender).isOnline()) {
			return;
		}
		for (String s : message) {
			if (StringUtils.isBlank(s)) {
				return;
			}
			commandSender.sendMessage(s);
		}
	}
}
