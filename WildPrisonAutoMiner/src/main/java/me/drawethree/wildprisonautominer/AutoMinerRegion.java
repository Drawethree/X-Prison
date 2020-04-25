package me.drawethree.wildprisonautominer;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@Getter
public class AutoMinerRegion {

	private WildPrisonAutoMiner parent;
	private ProtectedRegion region;
	private int moneyPerSecond;
	private int tokensPerSecond;

	private Task autoMinerTask;


	public AutoMinerRegion(WildPrisonAutoMiner parent, ProtectedRegion region, int moneyPerSecond, int tokensPerSecond) {
		this.parent = parent;
		this.region = region;
		this.moneyPerSecond = moneyPerSecond;
		this.tokensPerSecond = tokensPerSecond;

		this.autoMinerTask = Schedulers.async().runRepeating(() -> {
			for (Player p : Players.all()) {
				if (region.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())) {
					if (!parent.hasAutoMinerTime(p)) {
						sendActionBar(p, parent.getMessage("auto_miner_disabled"));
						continue;
					} else {
						sendActionBar(p, parent.getMessage("auto_miner_enabled"));
						WildPrisonTokens.getApi().addTokens(p, tokensPerSecond);
						this.parent.getEconomy().depositPlayer(p, moneyPerSecond);
						this.parent.decrementTime(p);
					}
				}
			}

		}, 20, 20);
	}

	private void sendActionBar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

}
