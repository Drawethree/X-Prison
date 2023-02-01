package dev.drawethree.xprison.nms;

import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class NMSProvider {

	public abstract void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics);

	public abstract void sendActionBar(Player player, String message);
}
