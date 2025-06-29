package dev.drawethree.xprison.mines.api;

import dev.drawethree.xprison.api.mines.XPrisonMinesAPI;
import dev.drawethree.xprison.api.mines.model.Mine;
import dev.drawethree.xprison.api.mines.model.MineSelection;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.mines.model.mine.MineImpl;
import org.bukkit.Location;

import java.util.Collection;
import java.util.stream.Collectors;

public final class XPrisonMinesAPIImpl implements XPrisonMinesAPI {

	private final XPrisonMines plugin;

	public XPrisonMinesAPIImpl(XPrisonMines plugin) {
		this.plugin = plugin;
	}

	@Override
	public Collection<Mine> getMines() {
		return this.plugin.getManager().getMines().stream().map(Mine.class::cast).collect(Collectors.toList());
	}

	@Override
	public MineImpl getMineByName(String name) {
		return this.plugin.getManager().getMineByName(name);
	}

	@Override
	public MineImpl getMineAtLocation(Location loc) {
		return this.plugin.getManager().getMineAtLocation(loc);
	}

	@Override
	public Mine createMine(MineSelection mineSelection, String name) {
		return this.plugin.getManager().createMine(mineSelection,name);
	}

	@Override
	public boolean deleteMine(Mine mine) {
		return this.plugin.getManager().deleteMine(mine);
	}

	@Override
	public void resetMine(Mine mine) {
		this.plugin.getManager().resetMine(mine);
	}
}
