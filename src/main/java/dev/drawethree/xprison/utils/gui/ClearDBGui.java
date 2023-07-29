package dev.drawethree.xprison.utils.gui;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ClearDBGui extends ConfirmationGui {

	private final XPrisonModule module;

	public ClearDBGui(Player player, XPrisonModule module) {
		super(player, module == null ? "Clear all player data?" : "Clear data for " + module.getName() + "?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			if (this.module == null) {
				this.getAllModules().forEach(XPrisonModule::resetPlayerData);
				PlayerUtils.sendMessage(this.getPlayer(), "&aX-Prison - All Modules Data have been reset.");
			} else {
				this.module.resetPlayerData();
				PlayerUtils.sendMessage(this.getPlayer(), "&aX-Prison - DB Player data for module " + module.getName() + " has been reset.");
			}
		}
		this.close();
	}

	private Collection<XPrisonModule> getAllModules() {
		return XPrison.getInstance().getModules();
	}
}
