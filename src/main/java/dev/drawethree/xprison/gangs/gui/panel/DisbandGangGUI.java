package dev.drawethree.xprison.gangs.gui.panel;

import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.utils.gui.ConfirmationGui;
import org.bukkit.entity.Player;

public final class DisbandGangGUI extends ConfirmationGui {

	private final XPrisonGangs plugin;
	private final GangImpl gangImpl;

	public DisbandGangGUI(XPrisonGangs plugin, Player player, GangImpl gangImpl) {
		super(player, plugin.getConfig().getGangDisbandGUITitle());
		this.plugin = plugin;
		this.gangImpl = gangImpl;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.plugin.getGangsManager().disbandGang(getPlayer(), this.gangImpl, false);
		}
		this.close();
	}
}
