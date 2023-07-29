package dev.drawethree.xprison.gangs.api;

import dev.drawethree.xprison.gangs.managers.GangsManager;
import dev.drawethree.xprison.gangs.model.Gang;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Optional;

public final class XPrisonGangsAPIImpl implements XPrisonGangsAPI {

	private final GangsManager gangsManager;

	public XPrisonGangsAPIImpl(GangsManager gangsManager) {
		this.gangsManager = gangsManager;
	}

	@Override
	public Optional<Gang> getPlayerGang(OfflinePlayer player) {
		return this.gangsManager.getPlayerGang(player);
	}

	@Override
	public Optional<Gang> getByName(String name) {
		return this.gangsManager.getGangWithName(name);
	}

	@Override
	public Collection<Gang> getAllGangs() {
		return this.gangsManager.getAllGangs();
	}
}
