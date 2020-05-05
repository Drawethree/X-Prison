package me.drawethree.wildprisoncore.autominer;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class WildPrisonAutoMiner {


    @Getter
    private FileManager.Config config;

    private HashMap<String, String> messages;
    private HashMap<UUID, Integer> autoMinerTimes;

    @Getter
    private AutoMinerRegion region;
    @Getter
    private WildPrisonCore core;

    public WildPrisonAutoMiner(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("autominer.yml").copyDefaults(true).save();
    }

    public void enable() {
        autoMinerTimes = new HashMap<>();
        this.registerCommands();
        this.loadMessages();
        this.loadAutoMinerRegion();
    }

    private void loadAutoMinerRegion() {
        String world = getConfig().get().getString("auto-miner-region.world");
        String regionName = getConfig().get().getString("auto-miner-region.name");
        int moneyPerSec = getConfig().get().getInt("auto-miner-region.money");
        int tokensPerSec = getConfig().get().getInt("auto-miner-region.tokens");

        ProtectedRegion region = WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
        if (region == null) {
            core.getLogger().warning(String.format("There is no such region named %s in world %s!", regionName, world));
            return;
        }
        this.region = new AutoMinerRegion(this, region, moneyPerSec, tokensPerSec);
        core.getLogger().info("AutoMiner region loaded!");

    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    public void disable() {

    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        c.sender().sendMessage(messages.get("auto_miner_time").replace("%time%", String.valueOf(autoMinerTimes.getOrDefault(c.sender().getUniqueId(), 0))));
                    }
                }).registerAndBind(core, "miner", "autominer");

        // /adminautominer give {Player} {Amount of time}
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 3 && c.rawArg(0).equalsIgnoreCase("give")) {
                        Player target = Players.getNullable(c.rawArg(1));
                        int time = c.arg(2).parseOrFail(Integer.class).intValue();
                        givePlayerAutoMinerTime(c.sender(), target, time);
                    }

                }).registerAndBind(core, "adminautominer", "aam");
    }

    private void givePlayerAutoMinerTime(CommandSender sender, Player p, int seconds) {

        if (p == null || !p.isOnline()) {
            sender.sendMessage(Text.colorize("&cPlayer is not online!"));
            return;
        }

        int currentSecs = autoMinerTimes.getOrDefault(p.getUniqueId(), 0);
        currentSecs += seconds;

        autoMinerTimes.put(p.getUniqueId(), currentSecs);
        sender.sendMessage(messages.get("auto_miner_time_add").replace("%time%", String.valueOf(seconds)).replace("%player%", p.getName()));
    }

    public boolean hasAutoMinerTime(Player p) {
        return autoMinerTimes.containsKey(p.getUniqueId()) && autoMinerTimes.get(p.getUniqueId()) > 0;
    }

    public void decrementTime(Player p) {
        autoMinerTimes.put(p.getUniqueId(), autoMinerTimes.get(p.getUniqueId()) - 1);
    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }
}
