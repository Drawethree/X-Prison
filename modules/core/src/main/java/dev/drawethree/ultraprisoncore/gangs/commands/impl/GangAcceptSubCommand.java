package dev.drawethree.ultraprisoncore.gangs.commands.impl;

import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.commands.GangSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class GangAcceptSubCommand extends GangSubCommand {

	public GangAcceptSubCommand(GangCommand command) {
		super(command, "accept", "join");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang accept";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player && args.size() == 0) {
			return this.command.getPlugin().getGangsManager().acceptInvite((Player) sender);
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
