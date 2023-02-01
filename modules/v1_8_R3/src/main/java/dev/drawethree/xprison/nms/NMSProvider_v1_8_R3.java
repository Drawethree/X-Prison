package dev.drawethree.xprison.nms;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class NMSProvider_v1_8_R3 extends NMSProvider {

	@Override
	public void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
		net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();
		BlockPosition bp = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
		nmsWorld.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
	}

	@Override
	public void sendActionBar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
