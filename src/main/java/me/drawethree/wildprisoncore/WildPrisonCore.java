package me.drawethree.wildprisoncore;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import me.drawethree.wildprisoncore.autojoin.WildPrisonAutoJoin;
import me.drawethree.wildprisoncore.autominer.WildPrisonAutoMiner;
import me.drawethree.wildprisoncore.autosell.WildPrisonAutoSell;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.drawethree.wildprisoncore.enchants.WildPrisonEnchants;
import me.drawethree.wildprisoncore.events.WildPrisonEvent;
import me.drawethree.wildprisoncore.events.impl.KeyAllEvent;
import me.drawethree.wildprisoncore.multipliers.WildPrisonMultipliers;
import me.drawethree.wildprisoncore.placeholders.WildPrisonPlaceholder;
import me.drawethree.wildprisoncore.ranks.WildPrisonRankup;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import me.jet315.prisonmines.JetsPrisonMines;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;


@Getter
public final class WildPrisonCore extends ExtendedJavaPlugin {

    private MySQLDatabase sqlDatabase;
    private Economy economy;
    private JetsPrisonMines jetsPrisonMines;
    private FileManager fileManager;
    private LuckPerms luckPerms;

    private WildPrisonTokens tokens;
    private WildPrisonRankup ranks;
    private WildPrisonMultipliers multipliers;
    private WildPrisonEnchants enchants;
    private WildPrisonAutoSell autoSell;
    private WildPrisonAutoMiner autoMiner;
    private WildPrisonAutoJoin autoJoin;

    private WildPrisonEvent keyAllEvent;

    @Override
    protected void enable() {
        this.fileManager = new FileManager(this);
        this.fileManager.getConfig("config.yml").copyDefaults(true).save();

        this.sqlDatabase = new MySQLDatabase(this);
        this.tokens = new WildPrisonTokens(this);
        this.ranks = new WildPrisonRankup(this);
        this.multipliers = new WildPrisonMultipliers(this);
        this.enchants = new WildPrisonEnchants(this);
        this.autoSell = new WildPrisonAutoSell(this);
        this.autoMiner = new WildPrisonAutoMiner(this);
        this.autoJoin = new WildPrisonAutoJoin(this);

        this.keyAllEvent = KeyAllEvent.getInstance();

        this.setupEconomy();
        this.luckPerms = LuckPermsProvider.get();
         // this.prisonMines = PrisonMines.getInstance();
        this.jetsPrisonMines = (JetsPrisonMines) getServer().getPluginManager().getPlugin("JetsPrisonMines");
        this.registerPlaceholders();

        this.tokens.enable();
        this.ranks.enable();
        this.multipliers.enable();
        this.enchants.enable();
        this.autoSell.enable();
        this.autoMiner.enable();
        this.autoJoin.enable();

        this.startEvents();
    }


    @Override
    protected void disable() {
        this.tokens.disable();
        this.ranks.disable();
        this.multipliers.disable();
        this.enchants.disable();
        this.autoSell.disable();
        this.autoMiner.disable();
        this.autoJoin.disable();

        this.sqlDatabase.close();
    }

    private void startEvents() {
        this.keyAllEvent.start();
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new WildPrisonPlaceholder(this).register();
        }
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

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
}
