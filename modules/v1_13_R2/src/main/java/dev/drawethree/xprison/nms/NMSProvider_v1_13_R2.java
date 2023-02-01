package dev.drawethree.xprison.nms;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.IBlockData;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.entity.Player;

public final class NMSProvider_v1_13_R2 extends NMSProvider {

	@Override
	public void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
		net.minecraft.server.v1_13_R2.World nmsWorld = ((CraftWorld) world).getHandle();
		BlockPosition bp = new BlockPosition(x, y, z);
		IBlockData ibd = net.minecraft.server.v1_13_R2.Block.getByCombinedId(blockId + (data << 12));
		nmsWorld.setTypeAndData(bp, ibd, applyPhysics ? 3 : 2);
	}

	@Override
	public void sendActionBar(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
	}
}
