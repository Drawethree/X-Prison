package dev.drawethree.xprison.tokens.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.xprison.tokens.managers.CommandManager;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class TokensHelpCommand extends TokensCommand {

	private static final String COMMAND_NAME = "help";
	private static final String[] COMMAND_ALIASES = {"?"};

	public TokensHelpCommand(CommandManager commandManager) {
		super(commandManager, COMMAND_NAME, COMMAND_ALIASES);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.isEmpty()) {
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			PlayerUtils.sendMessage(sender, "&e&lTOKEN HELP MENU ");
			PlayerUtils.sendMessage(sender, "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
			for (TokensCommand command : this.commandManager.getAll()) {
				if (command.canExecute(sender)) {
					PlayerUtils.sendMessage(sender, "&e" + command.getUsage());
				}
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

	@Override
	public String getUsage() {
		return "/tokens help - Displays all available commands.";
	}

	@Override
	public List<String> getTabComplete(List<String> args) {
		return new ArrayList<>();
	}
}
