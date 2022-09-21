package dev.drawethree.ultraprisoncore.mines.migration.utils;

import dev.drawethree.ultraprisoncore.mines.migration.exception.MinesMigrationNotSupportedException;
import dev.drawethree.ultraprisoncore.mines.migration.model.MinesMigration;
import dev.drawethree.ultraprisoncore.mines.migration.model.impl.JetsPrisonMinesMigration;
import dev.drawethree.ultraprisoncore.mines.migration.model.impl.MineResetLiteMigration;

public class MinesMigrationFactory {

	public static MinesMigration fromPlugin(String pluginName) throws MinesMigrationNotSupportedException {

		if ("jetsprisonmines".equalsIgnoreCase(pluginName)) {
			return new JetsPrisonMinesMigration();
		} else if ("mineresetlite".equalsIgnoreCase(pluginName)) {
			return new MineResetLiteMigration();
		}
		throw new MinesMigrationNotSupportedException(pluginName);

	}
}
