package me.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.commands.MineCommand;
import me.drawethree.ultraprisoncore.mines.migration.MinesMigration;
import org.bukkit.command.CommandSender;

public class MineMigrateCommand extends MineCommand {

	public MineMigrateCommand(UltraPrisonMines plugin) {
		super(plugin, "migrate");
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {

		if (args.size() != 1) {
			return false;
		}

		String pluginName = args.get(0);

		MinesMigration migration = MinesMigration.fromPlugin(pluginName);

		if (migration == null) {
			sender.sendMessage(this.plugin.getMessage("mine_migration_invalid_plugin"));
			return true;
		}

		migration.migrate(sender);
		return true;
	}

	@Override
	public String getUsage() {
		return "&cUsage: /mines migrate <plugin> - Migrates mines from other plugins";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(UltraPrisonMines.MINES_ADMIN_PERM);
	}
}
