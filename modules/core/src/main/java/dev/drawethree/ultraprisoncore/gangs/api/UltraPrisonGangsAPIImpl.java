package dev.drawethree.ultraprisoncore.gangs.api;

import dev.drawethree.ultraprisoncore.gangs.managers.GangsManager;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Optional;

public final class UltraPrisonGangsAPIImpl implements UltraPrisonGangsAPI {

	private final GangsManager gangsManager;

	public UltraPrisonGangsAPIImpl(GangsManager gangsManager) {
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
