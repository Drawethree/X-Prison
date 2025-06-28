package dev.drawethree.xprison.mainmenu.confirmation;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleAbstract;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import dev.drawethree.xprison.utils.gui.ConfirmationGui;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.entity.Player;

public class ResetModulePlayerDataConfirmationGui extends ConfirmationGui {
	private final XPrisonModuleAbstract module;

	public ResetModulePlayerDataConfirmationGui(Player player, XPrisonModuleAbstract module) {
		super(player, module == null ? "Reset all player data ?" : "Reset " + module.getName() + " player data?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {

			if (module == null) {
				XPrison.getInstance().getModules().stream().filter(module -> module instanceof PlayerDataHolder).map(PlayerDataHolder.class::cast).forEach(dev.drawethree.xprison.interfaces.PlayerDataHolder::resetPlayerData);
				PlayerUtils.sendMessage(this.getPlayer(), "&aSuccessfully reset player data of all modules.");
			} else {
				if (module instanceof PlayerDataHolder PlayerDataHolder) {
                    PlayerDataHolder.resetPlayerData();
				}
				PlayerUtils.sendMessage(this.getPlayer(), "&aSuccessfully reset player data of &e&l" + this.module.getName() + " &amodule.");
			}

		}
		this.close();
	}
}
