package dev.drawethree.ultraprisoncore.gangs.gui;

import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import org.bukkit.entity.Player;

public class DisbandGangGUI extends ConfirmationGui {

	private final UltraPrisonGangs plugin;

	public DisbandGangGUI(UltraPrisonGangs plugin, Player player) {
		super(player, plugin.getConfig().getGangDisbandGUITitle());
		this.plugin = plugin;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.plugin.getGangsManager().disbandGang(this.getPlayer());
		}
		this.close();
	}
}
