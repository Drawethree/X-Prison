package dev.drawethree.ultraprisoncore.utils.gui;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ClearDBGui extends ConfirmationGui {

	private final UltraPrisonModule module;

	public ClearDBGui(Player player, UltraPrisonModule module) {
		super(player, module == null ? "Clear all player data?" : "Clear data for " + module.getName() + "?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			if (this.module == null) {
				this.getAllModules().forEach(UltraPrisonModule::resetAllData);
				PlayerUtils.sendMessage(this.getPlayer(), "&aUltraPrisonCore - All Modules Data have been reset.");
			} else {
				this.module.resetAllData();
				PlayerUtils.sendMessage(this.getPlayer(), "&aUltraPrisonCore - DB Player data for module " + module.getName() + " has been reset.");
			}
		}
		this.close();
	}

	private Collection<UltraPrisonModule> getAllModules() {
		return UltraPrisonCore.getInstance().getModules();
	}
}
