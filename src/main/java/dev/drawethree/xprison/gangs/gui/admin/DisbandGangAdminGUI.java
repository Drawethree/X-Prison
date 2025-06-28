package dev.drawethree.xprison.gangs.gui.admin;

import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.utils.gui.ConfirmationGui;
import org.bukkit.entity.Player;

public final class DisbandGangAdminGUI extends ConfirmationGui {

	private final XPrisonGangs plugin;
	private final GangImpl gangImpl;

	public DisbandGangAdminGUI(XPrisonGangs plugin, Player player, GangImpl gangImpl) {
		super(player, "Disband " + gangImpl.getName() + " gang ?");
		this.plugin = plugin;
		this.gangImpl = gangImpl;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.plugin.getGangsManager().disbandGang(this.getPlayer(), this.gangImpl, true);
		}
		this.close();
	}
}
