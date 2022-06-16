package dev.drawethree.ultraprisoncore.mines.migration;

import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import org.bukkit.command.CommandSender;

public abstract class MinesMigration {

	protected UltraPrisonMines mines;
	protected String fromPlugin;

	MinesMigration(UltraPrisonMines mines, String fromPlugin) {
		this.mines = mines;
		this.fromPlugin = fromPlugin;
	}

	public abstract void migrate(CommandSender sender);
}
