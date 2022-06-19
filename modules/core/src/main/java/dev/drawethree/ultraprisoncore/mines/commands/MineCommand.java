package dev.drawethree.ultraprisoncore.mines.commands;

import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class MineCommand {

	protected UltraPrisonMines plugin;
	@Getter
	private final String name;
	@Getter
	private final String[] aliases;

	public MineCommand(UltraPrisonMines plugin, String name, String... aliases) {
		this.plugin = plugin;
		this.name = name;
		this.aliases = aliases;
	}

	public abstract boolean execute(CommandSender sender, List<String> args);

	public abstract String getUsage();

	public abstract boolean canExecute(CommandSender sender);
}
