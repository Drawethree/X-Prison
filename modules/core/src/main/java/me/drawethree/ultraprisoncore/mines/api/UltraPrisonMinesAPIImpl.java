package me.drawethree.ultraprisoncore.mines.api;

import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import org.bukkit.Location;

public class UltraPrisonMinesAPIImpl implements UltraPrisonMinesAPI {

	private UltraPrisonMines plugin;

	public UltraPrisonMinesAPIImpl(UltraPrisonMines plugin) {
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
