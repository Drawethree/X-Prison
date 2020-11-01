package me.drawethree.wildprisoncore.autominer.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class FuelCommand {

	public static final HashMap<String, FuelCommand> commands;

	static {
		commands = new HashMap<>();
		//commands.put("withdraw", new FuelWithdrawCommand(WildPrisonAutoMiner.getInstance()));
		commands.put("pay", new FuelPayCommand(WildPrisonAutoMiner.getInstance()));
		//commands.put("help", new FuelHelpCommand(WildPrisonAutoMiner.getInstance()));
	}

	protected WildPrisonAutoMiner plugin;

	public FuelCommand(WildPrisonAutoMiner plugin) {

		this.plugin = plugin;
	}

	public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

	public static FuelCommand getCommand(String arg) {
		return commands.get(arg.toLowerCase());
	}
}
