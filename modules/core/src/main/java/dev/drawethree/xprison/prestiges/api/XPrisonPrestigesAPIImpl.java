package dev.drawethree.xprison.prestiges.api;

import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.prestiges.model.Prestige;
import org.bukkit.entity.Player;

public final class XPrisonPrestigesAPIImpl implements XPrisonPrestigesAPI {

	private final XPrisonPrestiges plugin;

	public XPrisonPrestigesAPIImpl(XPrisonPrestiges plugin) {
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
