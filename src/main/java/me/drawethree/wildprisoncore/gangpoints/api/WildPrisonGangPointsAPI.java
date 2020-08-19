package me.drawethree.wildprisoncore.gangpoints.api;

import net.brcdev.gangs.gang.Gang;
import org.bukkit.entity.Player;

public interface WildPrisonGangPointsAPI {

	long getGangPoints(Gang gang);

	long getGangPoints(Player player);

	void setPoints(Gang gang, long amount);

	void removePoints(Gang gang, long amount);

	void addPoints(Gang gang, long amount);
}
