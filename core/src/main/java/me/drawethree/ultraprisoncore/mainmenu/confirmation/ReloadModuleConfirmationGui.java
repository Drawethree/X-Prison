package me.drawethree.ultraprisoncore.mainmenu.confirmation;

import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import me.lucko.helper.text3.Text;
import org.bukkit.entity.Player;

public class ReloadModuleConfirmationGui extends ConfirmationGui {

	private final UltraPrisonModule module;

	public ReloadModuleConfirmationGui(Player player, UltraPrisonModule module) {
		super(player, module == null ? "Reload all modules ?" : "Reload module " + module.getName() + "?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			if (module == null) {
				UltraPrisonCore.getInstance().getModules().forEach(module1 -> UltraPrisonCore.getInstance().reloadModule(module1));
				this.getPlayer().sendMessage(Text.colorize("&aSuccessfully reloaded all modules."));
			} else {
				UltraPrisonCore.getInstance().reloadModule(module);
				this.getPlayer().sendMessage(Text.colorize("&aSuccessfully reloaded &e&l" + this.module.getName() + " &amodule."));
			}
		}
		this.close();
	}
}
