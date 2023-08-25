package dev.drawethree.xprison.mines.migration.utils;

import dev.drawethree.xprison.mines.migration.exception.MinesMigrationNotSupportedException;
import dev.drawethree.xprison.mines.migration.model.MinesMigration;
import dev.drawethree.xprison.mines.migration.model.impl.MineResetLiteMigration;

public class MinesMigrationFactory {

	public static MinesMigration fromPlugin(String pluginName) throws MinesMigrationNotSupportedException {

		if ("mineresetlite".equalsIgnoreCase(pluginName)) {
			return new MineResetLiteMigration();
		}
		throw new MinesMigrationNotSupportedException(pluginName);

	}
}
