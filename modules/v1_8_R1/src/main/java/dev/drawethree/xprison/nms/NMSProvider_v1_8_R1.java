package dev.drawethree.xprison.nms;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class NMSProvider_v1_8_R1 extends NMSProvider {

	@Override
	public void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
		net.minecraft.server.v1_8_R1.World nmsWorld = ((CraftWorld) world).getHandle();
		BlockPosition bp = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_8_R1.Block.getByCombinedId(blockId + (data << 12));
		nmsWorld.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
	}

	@Override
	public void sendActionBar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
