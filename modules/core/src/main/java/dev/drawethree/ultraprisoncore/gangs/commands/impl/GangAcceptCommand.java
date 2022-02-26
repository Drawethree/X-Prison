package dev.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangAcceptCommand extends GangCommand {

	public GangAcceptCommand(UltraPrisonGangs plugin) {
		super(plugin, "accept", "join");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang accept";
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender instanceof Player && args.size() == 0) {
			return this.plugin.getGangsManager().acceptInvite((Player) sender);
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
