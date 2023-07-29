package dev.drawethree.xprison.history.api;

import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.history.XPrisonHistory;
import dev.drawethree.xprison.history.model.HistoryLine;
import org.bukkit.OfflinePlayer;

import java.util.List;

public final class XPrisonHistoryAPIImpl implements XPrisonHistoryAPI {

	private final XPrisonHistory plugin;

	public XPrisonHistoryAPIImpl(XPrisonHistory plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		return this.plugin.getHistoryManager().getPlayerHistory(player);
	}

	@Override
	public void createHistoryLine(OfflinePlayer player, XPrisonModule module, String context) {
		this.plugin.getHistoryManager().createPlayerHistoryLine(player, module, context);
	}
}
