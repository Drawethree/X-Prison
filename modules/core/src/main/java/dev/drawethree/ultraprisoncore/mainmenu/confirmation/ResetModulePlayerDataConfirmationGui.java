package dev.drawethree.ultraprisoncore.mainmenu.confirmation;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import org.bukkit.entity.Player;

public class ResetModulePlayerDataConfirmationGui extends ConfirmationGui {
	private final UltraPrisonModule module;

	public ResetModulePlayerDataConfirmationGui(Player player, UltraPrisonModule module) {
		super(player, module == null ? "Reset all player data ?" : "Reset " + module.getName() + " player data?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			if (module == null) {
				UltraPrisonCore.getInstance().getModules().forEach(UltraPrisonModule::resetPlayerData);
				PlayerUtils.sendMessage(this.getPlayer(), "&aSuccessfully reset player data of all modules.");
			} else {
				module.resetPlayerData();
				PlayerUtils.sendMessage(this.getPlayer(), "&aSuccessfully reset player data of &e&l" + this.module.getName() + " &amodule.");
			}

		}
		this.close();
	}
}
