package dev.drawethree.ultraprisoncore.ranks.api;

import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.ranks.model.Rank;
import org.bukkit.entity.Player;

public class UltraPrisonRanksAPIImpl implements UltraPrisonRanksAPI {

	private UltraPrisonRanks plugin;

	public UltraPrisonRanksAPIImpl(UltraPrisonRanks plugin) {
		this.plugin = plugin;
	}

	@Override
	public Rank getPlayerRank(Player p) {
		return plugin.getRankManager().getPlayerRank(p);
	}

	@Override
	public Rank getNextPlayerRank(Player player) {
		return plugin.getRankManager().getNextRank(this.getPlayerRank(player).getId());
	}

	@Override
	public int getRankupProgress(Player player) {
		return plugin.getRankManager().getRankupProgress(player);
	}

	@Override
	public void setPlayerRank(Player player, Rank rank) {
		plugin.getRankManager().setRank(player, rank, null);
	}
}
