package me.drawethree.wildprisoncore.ranks;

import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.drawethree.wildprisoncore.ranks.api.WildPrisonRankupAPI;
import me.drawethree.wildprisoncore.ranks.api.WildPrisonRankupAPIImpl;
import me.drawethree.wildprisoncore.ranks.manager.RankManager;
import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.HashMap;

@Getter
public final class WildPrisonRankup {

    @Getter
    private FileManager.Config config;

    private RankManager rankManager;

    @Getter
    private WildPrisonRankupAPI api;

    private HashMap<String, String> messages;

    @Getter
    private WildPrisonCore core;

    public WildPrisonRankup(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("ranks.yml").copyDefaults(true).save();
    }

    public void enable() {
        this.loadMessages();
        this.rankManager = new RankManager(this);
        api = new WildPrisonRankupAPIImpl(this);
        this.registerCommands();
        this.rankManager.loadAllData();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(this.getConfig().get().getString("messages." + key)));
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key.toLowerCase(), Text.colorize("&cMessage " + key + " not found."));
    }


    public void disable() {
        this.rankManager.stopUpdating();
        this.rankManager.saveAllDataSync();
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextRank(c.sender());
                    }
                }).registerAndBind(core, "rankup");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        boolean buy = this.rankManager.buyNextRank(c.sender());
                        while (buy) {
                            buy = this.rankManager.buyNextRank(c.sender());
                        }
                    }
                }).registerAndBind(core, "maxrankup", "mru");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextPrestige(c.sender());
                    }
                }).registerAndBind(core, "prestige");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        int amount = 0;
                        boolean buy = this.rankManager.buyNextPrestige(c.sender());
                        while (buy && 50 > amount) {
                            buy = this.rankManager.buyNextPrestige(c.sender());
                            amount++;
                        }
                    }
                }).registerAndBind(core, "maxprestige", "autoprestige");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.sendPrestigeTop(c.sender());
                    }
                }).registerAndBind(core, "prestigetop");
    }
}
