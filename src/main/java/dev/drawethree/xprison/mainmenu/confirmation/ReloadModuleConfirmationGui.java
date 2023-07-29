package dev.drawethree.xprison.mainmenu.confirmation;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.utils.gui.ConfirmationGui;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.entity.Player;

public class ReloadModuleConfirmationGui extends ConfirmationGui {

	private final XPrisonModule module;

	public ReloadModuleConfirmationGui(Player player, XPrisonModule module) {
		super(player, module == null ? "Reload all modules ?" : "Reload module " + module.getName() + "?");
		this.module = module;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			if (module == null) {
				XPrison.getInstance().getModules().forEach(module1 -> XPrison.getInstance().reloadModule(module1));
				XPrison.getInstance().getItemMigrator().reload();
				PlayerUtils.sendMessage(this.getPlayer(), "&aSuccessfully reloaded all modules.");
			} else {
				XPrison.getInstance().reloadModule(module);
				PlayerUtils.sendMessage(this.getPlayer(), "&aSuccessfully reloaded &e&l" + this.module.getName() + " &amodule.");
			}
		}
		this.close();
	}
}
