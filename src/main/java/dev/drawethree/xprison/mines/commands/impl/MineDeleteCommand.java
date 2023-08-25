package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MineDeleteCommand extends MineCommand {

	public MineDeleteCommand(XPrisonMines plugin) {
		super(plugin, "delete", "remove");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {

		if (args.size() != 1) {
			return false;
		}

		this.plugin.getManager().deleteMine(sender, args.get(0));
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines delete <name> - Delete a mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
