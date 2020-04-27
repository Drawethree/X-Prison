package me.drawethree.wildprisonrankup;

import lombok.Getter;
import me.drawethree.wildprisonrankup.api.WildPrisonRankupAPI;
import me.drawethree.wildprisonrankup.api.WildPrisonRankupAPIImpl;
import me.drawethree.wildprisonrankup.database.MySQLDatabase;
import me.drawethree.wildprisonrankup.manager.RankManager;
import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;

@Getter
public final class WildPrisonRankup extends ExtendedJavaPlugin {

    private Economy economy;
    private RankManager rankManager;
    @Getter
    private static WildPrisonRankupAPI api;
    private MySQLDatabase sqlDatabase;
    private HashMap<String, String> messages;

    @Override
    protected void load() {
        saveDefaultConfig();
    }

    @Override
    protected void enable() {
        this.saveDefaultConfig();
        this.rankManager = new RankManager(this);
        this.sqlDatabase = new MySQLDatabase(this);
        api = new WildPrisonRankupAPIImpl(this);

        this.setupEconomy();
        this.loadMessages();
        this.registerCommands();

        this.rankManager.loadAllData();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), Text.colorize(this.getConfig().getString("messages." + key)));
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key.toLowerCase(), Text.colorize("&cMessage " + key + " not found."));
    }

    @Override
    protected void disable() {
        this.rankManager.saveAllDataSync();
        this.sqlDatabase.close();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextRank(c.sender());
                    }
                }).registerAndBind(this, "rankup");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        boolean buy = this.rankManager.buyNextRank(c.sender());
                        while (buy) {
                            buy = this.rankManager.buyNextRank(c.sender());
                        }
                    }
                }).registerAndBind(this, "maxrankup", "mru");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.buyNextPrestige(c.sender());
                    }
                }).registerAndBind(this, "prestige");
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
                }).registerAndBind(this, "maxprestige", "autoprestige");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.rankManager.sendPrestigeTop(c.sender());
                    }
                }).registerAndBind(this, "prestigetop");
    }
}
