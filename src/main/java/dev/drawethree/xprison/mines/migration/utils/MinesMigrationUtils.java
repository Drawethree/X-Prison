package dev.drawethree.xprison.mines.migration.utils;

import dev.drawethree.xprison.mines.migration.gui.AllMinesMigrationGui;
import dev.drawethree.xprison.mines.migration.gui.MinesMigrationGui;
import dev.drawethree.xprison.mines.migration.model.MinesMigration;
import org.bukkit.entity.Player;

public class MinesMigrationUtils {

	public static void openMinesMigrationGui(Player player, MinesMigration migration) {
		MinesMigrationGui gui = new MinesMigrationGui(player, migration);
		gui.open();
	}

	public static void openAllMinesMigrationGui(Player player) {
		AllMinesMigrationGui gui = new AllMinesMigrationGui(player);
		gui.open();
	}


	private MinesMigrationUtils() {
		throw new UnsupportedOperationException("Cannot instantiate");
	}
}
