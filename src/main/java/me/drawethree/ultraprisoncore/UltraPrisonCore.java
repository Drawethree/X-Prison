package me.drawethree.ultraprisoncore;

import lombok.Getter;
import me.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import me.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.database.Database;
import me.drawethree.ultraprisoncore.database.DatabaseCredentials;
import me.drawethree.ultraprisoncore.database.SQLDatabase;
import me.drawethree.ultraprisoncore.database.implementations.MySQLDatabase;
import me.drawethree.ultraprisoncore.database.implementations.SQLiteDatabase;
import me.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.pickaxelevels.UltraPrisonPickaxeLevels;
import me.drawethree.ultraprisoncore.placeholders.UltraPrisonMVdWPlaceholder;
import me.drawethree.ultraprisoncore.placeholders.UltraPrisonPlaceholder;
import me.drawethree.ultraprisoncore.ranks.UltraPrisonRankup;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.drawethree.ultraprisoncore.utils.gui.ClearDBGui;
import me.jet315.prisonmines.JetsPrisonMines;
import me.jet315.prisonmines.JetsPrisonMinesAPI;
import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Getter
public final class UltraPrisonCore extends ExtendedJavaPlugin {


    private Map<String, UltraPrisonModule> loadedModules;

    @Getter
    private static UltraPrisonCore instance;
    private Database pluginDatabase;
    private Economy economy;
    private FileManager fileManager;

    private UltraPrisonTokens tokens;
    private UltraPrisonGems gems;
    private UltraPrisonRankup ranks;
    private UltraPrisonMultipliers multipliers;
    private UltraPrisonEnchants enchants;
    private UltraPrisonAutoSell autoSell;
    private UltraPrisonAutoMiner autoMiner;
    private UltraPrisonPickaxeLevels pickaxeLevels;

    private JetsPrisonMinesAPI jetsPrisonMinesAPI;

    @Override
    protected void enable() {

        instance = this;
        this.loadedModules = new HashMap<>();
        this.fileManager = new FileManager(this);
        this.fileManager.getConfig("config.yml").copyDefaults(true).save();

        try {
            String databaseType = this.getConfig().getString("database_type");

            if (databaseType.equalsIgnoreCase("sqlite")) {
                this.pluginDatabase = new SQLiteDatabase(this);
            } else if (databaseType.equalsIgnoreCase("mysql")) {
                this.pluginDatabase = new MySQLDatabase(this, DatabaseCredentials.fromConfig(this.getConfig()));
            } else {
                this.getLogger().warning(String.format("Error! Unknown database type: %s. Disabling plugin.", databaseType));
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }

        } catch (Exception e) {
            this.getLogger().warning("Could not maintain Database Connection. Disabling plugin.");

            e.printStackTrace();

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.tokens = new UltraPrisonTokens(this);
        this.gems = new UltraPrisonGems(this);
        this.ranks = new UltraPrisonRankup(this);
        this.multipliers = new UltraPrisonMultipliers(this);
        this.enchants = new UltraPrisonEnchants(this);
        this.autoSell = new UltraPrisonAutoSell(this);
        this.autoMiner = new UltraPrisonAutoMiner(this);
        this.pickaxeLevels = new UltraPrisonPickaxeLevels(this);

        this.setupEconomy();
        this.registerPlaceholders();
        this.registerJetsPrisonMines();

        if (this.getConfig().getBoolean("modules.tokens")) {
            this.loadModule(tokens);
        }
        if (this.getConfig().getBoolean("modules.gems")) {
            this.loadModule(gems);
        }
        if (this.getConfig().getBoolean("modules.ranks")) {
            this.loadModule(ranks);
        }
        if (this.getConfig().getBoolean("modules.multipliers")) {
            this.loadModule(multipliers);
        }
        if (this.getConfig().getBoolean("modules.enchants")) {
            this.loadModule(enchants);
        }
        if (this.getConfig().getBoolean("modules.autosell")) {
            this.loadModule(autoSell);
        }
        if (this.getConfig().getBoolean("modules.autominer")) {
            this.loadModule(autoMiner);
        }
        if (this.getConfig().getBoolean("modules.pickaxe_levels")) {
            this.loadModule(pickaxeLevels);
        }

        this.startEvents();
        this.registerMainCommand();
    }

    private void loadModule(UltraPrisonModule module) {
        this.loadedModules.put(module.getName(), module);
        module.enable();
        this.getLogger().info(Text.colorize(String.format("&aUltraPrisonCore - Module %s loaded.", module.getName())));
    }

    //Always unload via iterator!
    private void unloadModule(UltraPrisonModule module) {
        module.disable();
        this.getLogger().info(Text.colorize(String.format("&aUltraPrisonCore - Module %s unloaded.", module.getName())));
    }

    private void reloadModule(UltraPrisonModule module) {
        module.reload();
        this.getLogger().info(Text.colorize(String.format("&aUltraPrisonCore - Module %s reloaded.", module.getName())));

    }

    private void registerMainCommand() {
        Commands.create()
                .assertPermission("ultraprison.admin")
                .handler(c -> {
                    if (c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("reload")) {
                        this.reload(c.sender());
                    } else if (c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("cleardb")) {
                        if (c.sender() instanceof Player) {
                            new ClearDBGui(this.pluginDatabase, (Player) c.sender()).open();
                        } else {
                            this.pluginDatabase.resetAllData(c.sender());
                        }
                    } else if (c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("version") || c.rawArg(0).equalsIgnoreCase("v")) {
                        c.sender().sendMessage(Text.colorize("&7This server is running &f" + this.getDescription().getFullName()));
                    }
                }).registerAndBind(this, "prisoncore", "ultraprison", "prison", "ultraprisoncore", "upc");
    }

    private void reload(CommandSender sender) {
        for (UltraPrisonModule module : this.loadedModules.values()) {
            this.reloadModule(module);
        }
        sender.sendMessage(Text.colorize("&aUltraPrisonCore - Reloaded."));
    }


    @Override
    protected void disable() {

        Iterator<UltraPrisonModule> it = this.loadedModules.values().iterator();

        while (it.hasNext()) {
            this.unloadModule(it.next());
            it.remove();
        }

        if (this.pluginDatabase != null) {
            if (this.pluginDatabase instanceof SQLDatabase) {
                SQLDatabase sqlDatabase = (SQLDatabase) this.pluginDatabase;
                sqlDatabase.close();
            }
        }
    }

    private void startEvents() {

    }

    public boolean isModuleEnabled(String moduleName) {
        return this.loadedModules.containsKey(moduleName);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UltraPrisonPlaceholder(this).register();
        }

        new UltraPrisonMVdWPlaceholder(this);
    }

    private void registerJetsPrisonMines() {
        if (Bukkit.getPluginManager().getPlugin("JetsPrisonMines") != null) {
            this.jetsPrisonMinesAPI = ((JetsPrisonMines) getServer().getPluginManager().getPlugin("JetsPrisonMines")).getAPI();
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
}
