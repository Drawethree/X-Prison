package dev.drawethree.ultraprisoncore.mines.commands.impl;

import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import dev.drawethree.ultraprisoncore.mines.migration.utils.MinesMigrationUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class MineMigrateCommand extends MineCommand {

	public MineMigrateCommand(UltraPrisonMines plugin) {
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
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
