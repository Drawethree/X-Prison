package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import org.bukkit.command.CommandSender;

public abstract class GangCommand {

	protected UltraPrisonGangs plugin;
	@Getter
	private String name;
	@Getter
	private String[] aliases;

	public GangCommand(UltraPrisonGangs plugin, String name, String... aliases) {
		this.plugin = plugin;
		this.name = name;
		this.aliases = aliases;
	}

	public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

	public abstract String getUsage();

	public abstract boolean canExecute(CommandSender sender);
}
