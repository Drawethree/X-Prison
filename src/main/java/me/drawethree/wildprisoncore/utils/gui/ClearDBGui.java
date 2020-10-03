package me.drawethree.wildprisoncore.utils.gui;

import me.drawethree.wildprisoncore.database.MySQLDatabase;
import org.bukkit.entity.Player;

public class ClearDBGui extends ConfirmationGui {

	private final MySQLDatabase database;

	public ClearDBGui(MySQLDatabase database, Player player) {
		super(player, "Clear all SQL Tables ?");
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
