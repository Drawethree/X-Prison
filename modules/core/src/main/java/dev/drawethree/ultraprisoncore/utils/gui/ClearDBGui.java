package dev.drawethree.ultraprisoncore.utils.gui;

import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Schedulers;
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
						PlayerUtils.sendMessage(this.getPlayer(), "&aUltraPrisonCore - All Modules Data have been reset.");
					} else {
						PlayerUtils.sendMessage(this.getPlayer(), "&cUltraPrisonCore - Something went wrong during reseting data. Please check console.");
					}
				} else {
					this.database.resetData(module);
				}
			});
		}
		this.close();
	}
}
