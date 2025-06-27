package dev.drawethree.xprison.mines.migration.utils;

import dev.drawethree.xprison.mines.migration.exception.MinesMigrationNotSupportedException;
import dev.drawethree.xprison.mines.migration.model.MinesMigration;

public class MinesMigrationFactory {

	public static MinesMigration fromPlugin(String pluginName) throws MinesMigrationNotSupportedException {
		throw new MinesMigrationNotSupportedException(pluginName);
	}
}
