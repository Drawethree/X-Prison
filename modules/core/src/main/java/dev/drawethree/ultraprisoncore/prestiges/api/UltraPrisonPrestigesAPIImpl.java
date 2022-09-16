package dev.drawethree.ultraprisoncore.prestiges.api;

import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import dev.drawethree.ultraprisoncore.prestiges.model.Prestige;
import org.bukkit.entity.Player;

public class UltraPrisonPrestigesAPIImpl implements UltraPrisonPrestigesAPI {

	private final UltraPrisonPrestiges plugin;

	public UltraPrisonPrestigesAPIImpl(UltraPrisonPrestiges plugin) {
		this.plugin = plugin;
	}

	@Override
	public Prestige getPlayerPrestige(Player p) {
		return plugin.getPrestigeManager().getPlayerPrestige(p);
	}

	@Override
	public void setPlayerPrestige(Player player, Prestige prestige) {
		plugin.getPrestigeManager().setPlayerPrestige(null, player, prestige.getId());
	}

	@Override
	public void setPlayerPrestige(Player player, long prestige) {
		plugin.getPrestigeManager().setPlayerPrestige(null, player, prestige);

	}
}
