package dev.drawethree.ultraprisoncore.gangs.gui.panel;

import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import org.bukkit.entity.Player;

public final class DisbandGangGUI extends ConfirmationGui {

	private final UltraPrisonGangs plugin;
	private final Gang gang;

	public DisbandGangGUI(UltraPrisonGangs plugin, Player player, Gang gang) {
		super(player, plugin.getConfig().getGangDisbandGUITitle());
		this.plugin = plugin;
		this.gang = gang;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.plugin.getGangsManager().disbandGang(getPlayer(), this.gang, false);
		}
		this.close();
	}
}
