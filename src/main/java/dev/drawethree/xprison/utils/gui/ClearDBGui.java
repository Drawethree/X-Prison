package dev.drawethree.xprison.utils.gui;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleAbstract;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ClearDBGui extends ConfirmationGui {

	private final XPrisonModuleAbstract module;

	public ClearDBGui(Player player, XPrisonModuleAbstract module) {
		super(player, module == null ? "Clear all player data?" : "Clear data for " + module.getName() + "?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			if (this.module == null) {
				getAllModules().stream().filter(module -> module instanceof PlayerDataHolder).map(PlayerDataHolder.class::cast).forEach(dev.drawethree.xprison.interfaces.PlayerDataHolder::resetPlayerData);
				PlayerUtils.sendMessage(this.getPlayer(), "&aX-Prison - All Modules Data have been reset.");
			} else {
				PlayerDataHolder playerDataHolder = (PlayerDataHolder) module;
				playerDataHolder.resetPlayerData();
				PlayerUtils.sendMessage(this.getPlayer(), "&aX-Prison - DB Player data for module " + module.getName() + " has been reset.");
			}
		}
		this.close();
	}

	private Collection<XPrisonModuleAbstract> getAllModules() {
		return XPrison.getInstance().getModules();
	}
}
