package me.drawethree.ultraprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.text3.Text;
import org.bukkit.command.CommandSender;

public class TokensHelpCommand extends TokensCommand {

	public TokensHelpCommand(UltraPrisonTokens plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.isEmpty()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&lTOKEN HELP MENU "));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/tokens pay [player] [amount]"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/tokens withdraw [amount] [value]"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/tokens [player]"));
			if (sender.hasPermission(UltraPrisonTokens.TOKENS_ADMIN_PERM)) {
				PlayerUtils.sendMessage(sender, Text.colorize("&e/tokens give [player] [amount]"));
				PlayerUtils.sendMessage(sender, Text.colorize("&e/tokens remove [player] [amount]"));
				PlayerUtils.sendMessage(sender, Text.colorize("&e/tokens set [player] [amount]"));
			}
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			return true;
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
