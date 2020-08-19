package me.drawethree.wildprisoncore.gangpoints.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.gangpoints.WildPrisonGangPoints;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;


public class GangPointsHelpCommand extends GangPointsCommand {

	public GangPointsHelpCommand(WildPrisonGangPoints instance) {
		super(instance);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.isEmpty()) {
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			sender.sendMessage(Text.colorize("&e&lGANGPOINTS HELP MENU "));
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			if (sender.hasPermission(WildPrisonTokens.TOKENS_ADMIN_PERM)) {
				sender.sendMessage(Text.colorize("&e/gangpoints add [amount] [player/gang]"));
				sender.sendMessage(Text.colorize("&e/gangpoints remove [amount] [player/gang]"));
				sender.sendMessage(Text.colorize("&e/gangpoints set [amount] [player/gang]"));
			}
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			return true;
		}
		return false;
	}
}
