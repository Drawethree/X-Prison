package dev.drawethree.ultraprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;

public class TokensHelpCommand extends TokensCommand {

	public TokensHelpCommand(UltraPrisonTokens plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.isEmpty()) {
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e&lTOKEN HELP MENU ");
			PlayerUtils.sendMessage(sender,"&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e/tokens pay [player] [amount]");
			PlayerUtils.sendMessage(sender,"&e/tokens withdraw [amount] [value]");
			PlayerUtils.sendMessage(sender, "&e/tokens [player]");
			if (sender.hasPermission(UltraPrisonTokens.TOKENS_ADMIN_PERM)) {
				PlayerUtils.sendMessage(sender, "&e/tokens give [player] [amount]");
				PlayerUtils.sendMessage(sender,"&e/tokens remove [player] [amount]");
				PlayerUtils.sendMessage(sender, "&e/tokens set [player] [amount]");
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
