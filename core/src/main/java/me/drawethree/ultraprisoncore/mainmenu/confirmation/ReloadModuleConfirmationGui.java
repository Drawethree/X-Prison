package me.drawethree.ultraprisoncore.mainmenu.confirmation;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class ReloadModuleConfirmationGui extends ConfirmationGui {

	private final UltraPrisonModule module;

	public ReloadModuleConfirmationGui(Player player, UltraPrisonModule module) {
		super(player, "Reload module " + module.getName() + "?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			UltraPrisonCore.getInstance().reloadModule(module);
			this.getPlayer().sendMessage(Text.colorize("&aSuccessfully reloaded &e&l" + this.module.getName() + " &amodule."));
		}
		this.close();
	}
}
