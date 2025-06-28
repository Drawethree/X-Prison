package dev.drawethree.xprison.history.api;

import dev.drawethree.xprison.api.XPrisonModule;
import dev.drawethree.xprison.api.history.XPrisonHistoryAPI;
import dev.drawethree.xprison.api.history.model.HistoryLine;
import dev.drawethree.xprison.history.XPrisonHistory;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
	public Collection<HistoryLine> getPlayerHistory(OfflinePlayer offlinePlayer, XPrisonModule xPrisonModule) {
		return getPlayerHistory(offlinePlayer).stream().filter(historyLine -> historyLine.getModule().equals(xPrisonModule.getName())).collect(Collectors.toList());
	}

	@Override
	public HistoryLine createHistoryLine(OfflinePlayer player, XPrisonModule module, String context) {
		return this.plugin.getHistoryManager().createPlayerHistoryLine(player, module, context);
	}
}
