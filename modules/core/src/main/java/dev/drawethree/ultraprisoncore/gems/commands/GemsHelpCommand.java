package dev.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;

public class GemsHelpCommand extends GemsCommand {

	public GemsHelpCommand(UltraPrisonGems plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.isEmpty()) {
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e&lGEMS HELP MENU ");
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e/gems [player]");
			PlayerUtils.sendMessage(sender, "&e/gems pay [player] [amount]");
			PlayerUtils.sendMessage(sender, "&e/gems withdraw [amount] [value]");
			if (sender.hasPermission(UltraPrisonGems.GEMS_ADMIN_PERM)) {
				PlayerUtils.sendMessage(sender, "&e/gems give [player] [amount]");
				PlayerUtils.sendMessage(sender, "&e/gems remove [player] [amount]");
				PlayerUtils.sendMessage(sender, "&e/gems set [player] [amount]");
			}
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			return true;
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
