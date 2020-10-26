package me.drawethree.wildprisoncore.autominer;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class AutoMinerRegion {

    private WildPrisonAutoMiner parent;
    private World world;
    private ProtectedRegion region;
    private long moneyPerSecond;
    private long tokensPerSecond;
	private List<UUID> playersInRegion;

    private Task autoMinerTask;


    public AutoMinerRegion(WildPrisonAutoMiner parent, World world, ProtectedRegion region, long moneyPerSecond, long tokensPerSecond) {
        this.parent = parent;
        this.world = world;
        this.region = region;
        this.moneyPerSecond = moneyPerSecond;
        this.tokensPerSecond = tokensPerSecond;
		this.playersInRegion = new ArrayList<>();

        this.autoMinerTask = Schedulers.async().runRepeating(() -> {
            for (Player p : Players.all()) {

                WildPrisonAutoMiner.AutoMinerCommandLevel commandLevel = this.parent.getAutoMinerCommandLevel(p);
                WildPrisonAutoMiner.AutoMinerFuelLevel levelToGive = this.parent.getAutoMinerFuelLevel(p);

                if (levelToGive.getLevel() != parent.getLastLevel().getLevel()) {
                    if (!p.getWorld().equals(this.world)) {
                        continue;
                    }
                }

				if (this.region.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()) || (levelToGive.getLevel() == parent.getLastLevel().getLevel() && !this.parent.hasAutominerOff(p))) {

					if (!this.playersInRegion.contains(p.getUniqueId()) && this.region.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())) {
						for (Player p1 : Players.all()) {
							if (p1 != p) {
								Schedulers.sync().run(() -> p1.hidePlayer(this.parent.getCore(), p));
							}
						}
						this.playersInRegion.add(p.getUniqueId());
					}

					if (!this.parent.hasAutoMinerFuel(p) || this.parent.getPlayerFuel(p) < levelToGive.getFuelConsume()) {
						sendActionBar(p, this.parent.getMessage("auto_miner_disabled"));
						continue;
					} else {
						sendActionBar(p, this.parent.getMessage("auto_miner_enabled"));

						//Autominer should not be affected by LuckyBooster enchant
						this.parent.getCore().getTokens().getApi().addTokens(p, (long) levelToGive.getTokensPerSec());
						this.parent.getCore().getEconomy().depositPlayer(p, levelToGive.getMoneyPerSec());
						this.parent.getCore().getAutoSell().addToCurrentEarnings(p, levelToGive.getMoneyPerSec());
						this.parent.decrementFuel(p, levelToGive.getFuelConsume());

						commandLevel.giveRewards(p);

					}
				} else {
					if (this.playersInRegion.contains(p.getUniqueId())) {
						for (Player p1 : Players.all()) {
							if (p1 != p) {
								Schedulers.sync().run(() -> p1.showPlayer(this.parent.getCore(), p));
							}
						}
						this.playersInRegion.remove(p.getUniqueId());
					}
				}
			}
        }, 20, 20);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
		/*PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message),(byte) 2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);*/
    }

}
