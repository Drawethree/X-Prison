package dev.drawethree.xprison.mines.commands.impl;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.commands.MineCommand;
import dev.drawethree.xprison.mines.migration.utils.MinesMigrationUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class MineMigrateCommand extends MineCommand {

	public MineMigrateCommand(XPrisonMines plugin) {
		super(plugin, "migrate");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player) {
			MinesMigrationUtils.openAllMinesMigrationGui((Player) sender);
		} else {
			sender.sendMessage("This command can only be executed from game.");
		}
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines migrate - Opens Migration GUI";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(XPrisonMines.MINES_ADMIN_PERM);
	}
}
