package me.drawethree.ultraprisoncore.nms;

import org.bukkit.World;

public abstract class NMSProvider {
	public abstract void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics);
}
