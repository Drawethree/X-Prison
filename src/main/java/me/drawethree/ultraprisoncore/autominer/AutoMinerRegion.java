package me.drawethree.ultraprisoncore.autominer;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.utils.Players;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Getter
public class AutoMinerRegion {

    private UltraPrisonAutoMiner parent;
    private World world;
    private ProtectedRegion region;
    private long moneyPerSecond;
    private long tokensPerSecond;

    private Task autoMinerTask;


    public AutoMinerRegion(UltraPrisonAutoMiner parent, World world, ProtectedRegion region, long moneyPerSecond, long tokensPerSecond) {
        this.parent = parent;
        this.world = world;
        this.region = region;
        this.moneyPerSecond = moneyPerSecond;
        this.tokensPerSecond = tokensPerSecond;

        this.autoMinerTask = Schedulers.async().runRepeating(() -> {
            for (Player p : Players.all()) {

                if (!p.getWorld().equals(this.world)) {
                    continue;
                }

                if (region.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())) {
                    if (!parent.hasAutoMinerTime(p)) {
                        sendActionBar(p, parent.getMessage("auto_miner_disabled"));
                        continue;
                    } else {
                        sendActionBar(p, parent.getMessage("auto_miner_enabled"));
                        parent.getCore().getTokens().getApi().addTokens(p, tokensPerSecond);
                        this.parent.getCore().getEconomy().depositPlayer(p, moneyPerSecond);
                        this.parent.decrementTime(p);
                    }
                }
            }
        }, 20, 20);
    }

    private void sendActionBar(Player player, String message) {
        ActionBarAPI.sendActionBar(player,message);
    }

}
