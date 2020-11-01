package me.drawethree.wildprisoncore.autominer.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

public class FuelHelpCommand extends FuelCommand {

	public FuelHelpCommand(WildPrisonAutoMiner plugin) {
		super(plugin);
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.isEmpty()) {
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			sender.sendMessage(Text.colorize("&e&lFUEL HELP MENU "));
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			sender.sendMessage(" ");
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			return true;
		}
		return false;
	}
}
