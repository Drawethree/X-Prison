package me.drawethree.ultraprisoncore.utils.gui;

import me.drawethree.ultraprisoncore.database.MySQLDatabase;
import org.bukkit.entity.Player;

public class ClearDBGui extends ConfirmationGui {

	private final MySQLDatabase database;

	public ClearDBGui(MySQLDatabase database, Player player) {
		super(player, "Clear all player data?");
		this.database = database;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.database.resetAllTables(this.getPlayer());
		}
		this.close();
	}
}
