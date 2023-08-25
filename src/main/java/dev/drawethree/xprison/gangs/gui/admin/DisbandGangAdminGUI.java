package dev.drawethree.xprison.gangs.gui.admin;

import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.utils.gui.ConfirmationGui;
import org.bukkit.entity.Player;

public final class DisbandGangAdminGUI extends ConfirmationGui {

	private final XPrisonGangs plugin;
	private final Gang gang;

	public DisbandGangAdminGUI(XPrisonGangs plugin, Player player, Gang gang) {
		super(player, "Disband " + gang.getName() + " gang ?");
		this.plugin = plugin;
		this.gang = gang;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.plugin.getGangsManager().disbandGang(this.getPlayer(), this.gang, true);
		}
		this.close();
	}
}
