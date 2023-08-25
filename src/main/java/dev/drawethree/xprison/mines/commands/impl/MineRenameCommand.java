package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MineRenameCommand extends MineCommand {

	public MineRenameCommand(XPrisonMines plugin) {
		super(plugin, "rename");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() != 2) {
			return false;
		}

		if (!(sender instanceof Player)) {
			return false;
		}

		this.plugin.getManager().renameMine((Player) sender, args.get(0), args.get(1));
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines rename <name> <new_name> - Renames a mine";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
