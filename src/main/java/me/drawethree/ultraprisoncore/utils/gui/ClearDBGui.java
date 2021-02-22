package me.drawethree.ultraprisoncore.utils.gui;

import me.drawethree.ultraprisoncore.database.Database;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
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
			Schedulers.async().run(() -> {
				this.database.resetAllData(this.getPlayer());
				this.getPlayer().sendMessage(Text.colorize("&aUltraPrisonCore - Players Data have been reset."));
			});
		}
		this.close();
	}
}
