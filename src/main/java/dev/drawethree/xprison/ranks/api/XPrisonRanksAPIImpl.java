package dev.drawethree.xprison.ranks.api;

import dev.drawethree.xprison.api.ranks.XPrisonRanksAPI;
import dev.drawethree.xprison.api.ranks.model.Rank;
import dev.drawethree.xprison.ranks.manager.RanksManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class XPrisonRanksAPIImpl implements XPrisonRanksAPI {

	private final RanksManager manager;

	public XPrisonRanksAPIImpl(RanksManager manager) {
		this.manager = manager;
	}

	@Override
	public Rank getRankById(int i) {
		return manager.getRankById(i).orElse(null);
	}

	@Override
	public Rank getPlayerRank(Player p) {
		return manager.getPlayerRank(p);
	}

	@Override
	public Optional<Rank> getNextPlayerRank(Player player) {
		return manager.getNextRank(this.getPlayerRank(player).getId()).map(Rank.class::cast);
	}

	@Override
	public int getRankupProgress(Player player) {
		return manager.getRankupProgress(player);
	}

	@Override
	public void setPlayerRank(Player player, Rank rank) {
		manager.setRank(player, rank, null);
	}

	@Override
	public void resetPlayerRank(Player player) {
		manager.resetPlayerRank(player);
	}

	@Override
	public boolean isMaxRank(Player player) {
		return manager.isMaxRank(player);
	}
}
