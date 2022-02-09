package me.drawethree.ultraprisoncore.utils.gui;

import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.database.Database;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class ClearDBGui extends ConfirmationGui {

	private final Database database;
	private final UltraPrisonModule module;

	public ClearDBGui(Database database, Player player, UltraPrisonModule module) {
		super(player, module == null ? "Clear all player data?" : "Clear data for " + module.getName() + "?");
		this.database = database;
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			Schedulers.async().run(() -> {
				if (this.module == null) {
					if (this.database.resetAllData()) {
						PlayerUtils.sendMessage(this.getPlayer(), Text.colorize("&aUltraPrisonCore - All Modules Data have been reset."));
					} else {
						PlayerUtils.sendMessage(this.getPlayer(), Text.colorize("&cUltraPrisonCore - Something went wrong during reseting data. Please check console."));
					}
				} else {
					this.database.resetData(module);
				}
			});
		}
		this.close();
	}
}
