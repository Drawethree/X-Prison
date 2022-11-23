package dev.drawethree.ultraprisoncore.gangs.gui.admin;

import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import org.bukkit.entity.Player;

public final class DisbandGangAdminGUI extends ConfirmationGui {

	private final UltraPrisonGangs plugin;
	private final Gang gang;

	public DisbandGangAdminGUI(UltraPrisonGangs plugin, Player player, Gang gang) {
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
