package me.drawethree.wildprisoncore.gangpoints.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.gangpoints.WildPrisonGangPoints;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class GangPointsCommand {

	public static final HashMap<String, GangPointsCommand> commands;

	static {
		commands = new HashMap<>();
		commands.put("add", new GangPointsAddCommand(WildPrisonGangPoints.getInstance()));
		commands.put("remove", new GangPointsRemoveCommand(WildPrisonGangPoints.getInstance()));
		commands.put("set", new GangPointsSetCommand(WildPrisonGangPoints.getInstance()));
		commands.put("help", new GangPointsHelpCommand(WildPrisonGangPoints.getInstance()));
	}

	protected WildPrisonGangPoints plugin;

	public GangPointsCommand(WildPrisonGangPoints plugin) {
		this.plugin = plugin;
	}

	public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

	public static GangPointsCommand getCommand(String arg) {
		return commands.get(arg.toLowerCase());
	}
}
