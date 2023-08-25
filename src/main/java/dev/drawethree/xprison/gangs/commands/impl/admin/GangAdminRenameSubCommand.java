package dev.drawethree.xprison.gangs.commands.impl.admin;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.gangs.utils.GangsConstants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public final class GangAdminRenameSubCommand extends GangSubCommand {

	public GangAdminRenameSubCommand(GangCommand command) {
		super(command, "rename");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 2) {
			String oldGangName = args.get(0);
			String newGangName = args.get(1);
			return this.command.getPlugin().getGangsManager().forceRename(sender, oldGangName, newGangName);
		}
		return false;
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang admin rename <gang> <new_name>";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(GangsConstants.GANGS_ADMIN_PERM);
	}

	@Override
	public List<String> getTabComplete() {
		return this.command.getPlugin().getGangsManager().getAllGangs().stream().map(Gang::getName).collect(Collectors.toList());
	}
}