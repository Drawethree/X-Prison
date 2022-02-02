package me.drawethree.ultraprisoncore.history.gui.confirmation;

import me.drawethree.ultraprisoncore.history.UltraPrisonHistory;
import me.drawethree.ultraprisoncore.utils.gui.ConfirmationGui;
import me.lucko.helper.text3.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerClearHistoryConfirmationGUI extends ConfirmationGui {

	private final OfflinePlayer target;
	private UltraPrisonHistory plugin;

	public PlayerClearHistoryConfirmationGUI(Player player, OfflinePlayer target, UltraPrisonHistory plugin) {
		super(player, "Clear " + target.getName() + "?");
		this.target = target;
		this.plugin = plugin;
	}

	@Override
	public void confirm(boolean confirm) {
		if (confirm) {
			this.plugin.getHistoryManager().clearPlayerHistory(this.target);
			this.getPlayer().sendMessage(Text.colorize("&aYou have cleared history data of player &e" + target.getName()));
		}
		this.close();
	}
}
