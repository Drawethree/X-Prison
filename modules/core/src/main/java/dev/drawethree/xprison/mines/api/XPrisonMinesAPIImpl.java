package dev.drawethree.xprison.mines.api;

import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.Mine;
import org.bukkit.Location;

public final class XPrisonMinesAPIImpl implements XPrisonMinesAPI {

	private final XPrisonMines plugin;

	public XPrisonMinesAPIImpl(XPrisonMines plugin) {
		this.plugin = plugin;
	}

	@Override
	public Mine getMineByName(String name) {
		return this.plugin.getManager().getMineByName(name);
	}

	@Override
	public Mine getMineAtLocation(Location loc) {
		return this.plugin.getManager().getMineAtLocation(loc);
	}
}
