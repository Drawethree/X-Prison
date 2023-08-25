package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MineToolCommand extends MineCommand {

	public MineToolCommand(XPrisonMines plugin) {
		super(plugin, "tool");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		if (args.size() != 0) {
			return false;
		}

		return this.plugin.getManager().giveTool((Player) sender);
	}

	@Override
	public String getUsage() {
		return "/mines tool - Gives you a selection tool";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
