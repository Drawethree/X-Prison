package dev.drawethree.xprison.gangs.api;

import dev.drawethree.xprison.api.gangs.XPrisonGangsAPI;
import dev.drawethree.xprison.api.gangs.enums.GangCreateResult;
import dev.drawethree.xprison.api.gangs.enums.GangNameCheckResult;
import dev.drawethree.xprison.api.gangs.model.Gang;
import dev.drawethree.xprison.gangs.managers.GangsManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public final class XPrisonGangsAPIImpl implements XPrisonGangsAPI {

	private final GangsManager gangsManager;

	public XPrisonGangsAPIImpl(GangsManager gangsManager) {
		this.gangsManager = gangsManager;
	}

	@Override
	public Optional<Gang> getPlayerGang(OfflinePlayer player) {
		return this.gangsManager.getPlayerGang(player).map(Gang.class::cast);
	}

	@Override
	public Optional<Gang> getByName(String name) {
		return this.gangsManager.getGangWithName(name).map(Gang.class::cast);
	}

	@Override
	public Collection<Gang> getAllGangs() {
		return this.gangsManager.getAllGangs().stream().map(Gang.class::cast).collect(Collectors.toList());
	}

	@Override
	public GangCreateResult createGang(String s, Player gangLeader) {
		return this.gangsManager.createGang(s,gangLeader);
	}

	@Override
	public void disbandGang(Gang gang) {
		this.gangsManager.disbandGang(null,gang,true);
	}

	@Override
	public GangNameCheckResult checkGangName(String s) {
		return this.gangsManager.performNameCheck(s,null);
	}
}
