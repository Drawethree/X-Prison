package me.drawethree.ultraprisoncore.mainmenu.confirmation;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class ResetModulePlayerDataConfirmationGui extends ConfirmationGui {
	private final UltraPrisonModule module;

	public ResetModulePlayerDataConfirmationGui(Player player, UltraPrisonModule module) {
		super(player, "Reset " + module.getName() + " player data?");

		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			UltraPrisonCore.getInstance().getPluginDatabase().resetData(this.module);
			this.getPlayer().sendMessage(Text.colorize("&aSuccessfully reset player data of &e&l" + this.module.getName() + " &amodule."));
		}
		this.close();
	}
}
