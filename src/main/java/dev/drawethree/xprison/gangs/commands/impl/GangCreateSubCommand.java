package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.enums.GangCreateResult;
import dev.drawethree.xprison.gangs.utils.GangsConstants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class GangCreateSubCommand extends GangSubCommand {

	public GangCreateSubCommand(GangCommand command) {
		super(command, "create", "new");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang create <name>";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player && args.size() == 1) {
			return this.command.getPlugin().getGangsManager().createGang(args.get(0), (Player) sender) == GangCreateResult.SUCCESS;
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(GangsConstants.GANGS_CREATE_PERM);
	}

	@Override
	public List<String> getTabComplete() {
		return new ArrayList<>();
	}
}
