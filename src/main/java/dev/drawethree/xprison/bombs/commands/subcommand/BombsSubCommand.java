package dev.drawethree.xprison.bombs.commands.subcommand;

import dev.drawethree.xprison.bombs.commands.BombsCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BombsSubCommand {

	@Getter
	protected final BombsCommand command;
	@Getter
	private final String[] aliases;

	protected final Map<String, GangSubCommand> subCommands;

	public BombsSubCommand(BombsCommand command, String... aliases) {
		this.command = command;
		this.aliases = aliases;
		this.subCommands = new HashMap<>();
	}

	public abstract boolean execute(CommandSender sender, List<String> args);

	public abstract String getUsage();

	public abstract List<String> getTabComplete();

	protected void registerSubCommand(GangSubCommand subCommand) {
		for (String alias : subCommand.getAliases()) {
			this.subCommands.put(alias.toLowerCase(), subCommand);
		}
	}

	protected GangSubCommand getSubCommand(String name) {
		return subCommands.get(name.toLowerCase());
	}
}
