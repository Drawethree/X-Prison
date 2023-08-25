package dev.drawethree.xprison.mines.api;

import dev.drawethree.xprison.mines.model.mine.Mine;
import org.bukkit.Location;

public interface XPrisonMinesAPI {

	/**
	 * Gets a mine by name
	 *
	 * @param name String
	 * @return Mine.class
	 */
	Mine getMineByName(String name);

	/**
	 * Gets a mine by location
	 *
	 * @param loc Location
	 * @return Mine.class
	 */
	Mine getMineAtLocation(Location loc);
}
