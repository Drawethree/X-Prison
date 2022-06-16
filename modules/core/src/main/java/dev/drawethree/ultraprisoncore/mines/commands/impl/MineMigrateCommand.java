package dev.drawethree.ultraprisoncore.mines.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.commands.MineCommand;
import dev.drawethree.ultraprisoncore.mines.migration.MinesMigration;
import dev.drawethree.ultraprisoncore.mines.migration.MinesMigrationFactory;
import dev.drawethree.ultraprisoncore.mines.migration.MinesMigrationNotSupportedException;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
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
		MinesMigration migration;

		try {
			migration = this.getMinesMigrationFromPluginName(pluginName);
		} catch (MinesMigrationNotSupportedException e) {
			PlayerUtils.sendMessage(sender, this.plugin.getMessage("mine_migration_invalid_plugin"));
			return true;
		}

		migration.migrate(sender);
		return true;
	}

	private MinesMigration getMinesMigrationFromPluginName(String pluginName) throws MinesMigrationNotSupportedException {
		return MinesMigrationFactory.fromPlugin(pluginName);
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
