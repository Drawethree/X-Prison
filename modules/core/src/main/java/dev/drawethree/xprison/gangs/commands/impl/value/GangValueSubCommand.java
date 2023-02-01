package dev.drawethree.xprison.gangs.commands.impl.value;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.utils.GangsConstants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class GangValueSubCommand extends GangSubCommand {

	public GangValueSubCommand(GangCommand command) {
		super(command, "value");
		registerSubCommand(new GangValueAddSubCommand(command));
		registerSubCommand(new GangValueRemoveSubCommand(command));
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang value <add/remove> <gang/player> <amount>";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() > 0) {
			GangSubCommand subCommand = getSubCommand(args.get(0));
			if (subCommand != null) {
				return subCommand.execute(sender, args.subList(1, args.size()));
			}
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(GangsConstants.GANGS_ADMIN_PERM);
	}

	@Override
	public List<String> getTabComplete() {
		return new ArrayList<>(this.subCommands.keySet());
	}
}
