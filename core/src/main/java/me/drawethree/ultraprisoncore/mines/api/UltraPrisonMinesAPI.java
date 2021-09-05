package me.drawethree.ultraprisoncore.mines.api;

import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import org.bukkit.Location;

public interface UltraPrisonMinesAPI {

	Mine getMineByName(String name);

	Mine getMineAtLocation(Location loc);
}
