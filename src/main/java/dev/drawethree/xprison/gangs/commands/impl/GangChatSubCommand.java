package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public final class GangChatSubCommand extends GangSubCommand {

	public GangChatSubCommand(GangCommand command) {
		super(command, "chat", "c");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang chat";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 0 && sender instanceof Player) {
			Player p = (Player) sender;
			return this.command.getPlugin().getGangsManager().toggleGangChat(p);
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
