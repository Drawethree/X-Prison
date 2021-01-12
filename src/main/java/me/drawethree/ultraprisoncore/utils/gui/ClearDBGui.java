package me.drawethree.ultraprisoncore.utils.gui;

import me.drawethree.ultraprisoncore.database.Database;
import org.bukkit.entity.Player;

public class ClearDBGui extends ConfirmationGui {

	private final Database database;

	public ClearDBGui(Database database, Player player) {
		super(player, "Clear all player data?");
		this.database = database;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.database.resetAllData(this.getPlayer());
		}
		this.close();
	}
}
