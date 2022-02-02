package me.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GangTopCommand extends GangCommand {

	public GangTopCommand(UltraPrisonGangs plugin) {
		super(plugin, "top", "leaderboard");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang top";
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (args.size() == 0) {
			return this.plugin.getGangsManager().sendGangTop(sender);
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
