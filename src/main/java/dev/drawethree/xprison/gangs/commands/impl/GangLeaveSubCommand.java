package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.enums.GangLeaveReason;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class GangLeaveSubCommand extends GangSubCommand {

	public GangLeaveSubCommand(GangCommand command) {
		super(command, "leave", "quit");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang leave";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 0 && sender instanceof Player) {
			Player p = (Player) sender;
			return this.command.getPlugin().getGangsManager().leaveGang(p, GangLeaveReason.LEAVE);
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabComplete() {
		return new ArrayList<>();
	}
}
