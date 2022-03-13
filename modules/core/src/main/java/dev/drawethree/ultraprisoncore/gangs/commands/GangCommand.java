package dev.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import lombok.Getter;
import org.bukkit.command.CommandSender;

public abstract class GangCommand {

	protected UltraPrisonGangs plugin;
	@Getter
	private final String name;
	@Getter
	private final String[] aliases;

	public GangCommand(UltraPrisonGangs plugin, String name, String... aliases) {
		this.plugin = plugin;
		this.name = name;
		this.aliases = aliases;
	}

	public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

	public abstract String getUsage();

	public abstract boolean canExecute(CommandSender sender);
}
