package me.drawethree.ultraprisoncore.gangs.api;

import me.drawethree.ultraprisoncore.gangs.managers.GangsManager;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class UltraPrisonGangsAPIImpl implements UltraPrisonGangsAPI {

	private GangsManager gangsManager;

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
}
