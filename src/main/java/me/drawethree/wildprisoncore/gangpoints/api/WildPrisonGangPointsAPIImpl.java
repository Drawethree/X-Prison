package me.drawethree.wildprisoncore.gangpoints.api;

import me.drawethree.wildprisoncore.gangpoints.managers.GangPointsManager;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.entity.Player;

public class WildPrisonGangPointsAPIImpl implements WildPrisonGangPointsAPI {

	private GangPointsManager gangPointsManager;

	public WildPrisonGangPointsAPIImpl(GangPointsManager gangPointsManager) {

		this.gangPointsManager = gangPointsManager;
	}

	@Override
	public long getGangPoints(Gang gang) {
		return this.gangPointsManager.getGangPoints(gang);
	}

	@Override
	public long getGangPoints(Player player) {
		return this.gangPointsManager.getGangPoints(player);
	}

	@Override
	public void setPoints(Gang gang, long amount) {
		this.gangPointsManager.setPoints(gang, amount, null);
	}

	@Override
	public void removePoints(Gang gang, long amount) {
		this.gangPointsManager.removePoints(gang, amount, null);
	}

	@Override
	public void addPoints(Gang gang, long amount) {
		this.gangPointsManager.addPoints(gang, amount, null);
	}
}
