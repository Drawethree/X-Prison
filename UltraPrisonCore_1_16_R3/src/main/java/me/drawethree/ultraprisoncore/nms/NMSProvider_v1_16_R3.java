package me.drawethree.ultraprisoncore.nms;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class NMSProvider_v1_16_R3 extends NMSProvider {

	@Override
	public void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
		net.minecraft.server.v1_16_R3.World nmsWorld = ((CraftWorld) world).getHandle();
		BlockPosition bp = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_16_R3.Block.getByCombinedId(blockId + (data << 12));
		nmsWorld.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
	}
}
