package dev.drawethree.xprison.prestiges.api;

import dev.drawethree.xprison.api.prestiges.XPrisonPrestigesAPI;
import dev.drawethree.xprison.api.prestiges.model.Prestige;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.prestiges.model.PrestigeImpl;
import org.bukkit.entity.Player;

public final class XPrisonPrestigesAPIImpl implements XPrisonPrestigesAPI {

	private final XPrisonPrestiges plugin;

	public XPrisonPrestigesAPIImpl(XPrisonPrestiges plugin) {
		this.plugin = plugin;
	}

	@Override
	public Prestige getPrestigeById(long l) {
		return plugin.getPrestigeManager().getPrestigeById(l);
	}

	@Override
	public PrestigeImpl getPlayerPrestige(Player p) {
		return plugin.getPrestigeManager().getPlayerPrestige(p);
	}

	@Override
	public void setPlayerPrestige(Player player, Prestige prestigeImpl) {
		plugin.getPrestigeManager().setPlayerPrestige(null, player, prestigeImpl.getId());
	}

	@Override
	public void setPlayerPrestige(Player player, long prestige) {
		plugin.getPrestigeManager().setPlayerPrestige(null, player, prestige);

	}

	@Override
	public boolean isMaxPrestige(Player player) {
		return false;
	}
}
