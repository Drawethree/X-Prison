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
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.help.HelpGui;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.nms.NMSProvider;
import me.drawethree.ultraprisoncore.pickaxelevels.UltraPrisonPickaxeLevels;
import me.drawethree.ultraprisoncore.placeholders.UltraPrisonMVdWPlaceholder;
import me.drawethree.ultraprisoncore.placeholders.UltraPrisonPAPIPlaceholder;
import me.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.drawethree.ultraprisoncore.utils.SkullUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.drawethree.ultraprisoncore.utils.gui.ClearDBGui;
import me.jet315.prisonmines.JetsPrisonMines;
import me.jet315.prisonmines.JetsPrisonMinesAPI;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public final class UltraPrisonCore extends ExtendedJavaPlugin {


	private static final int METRICS_SERVICE_ID = 10520;
	private static boolean DEBUG_MODE;

	private LinkedHashMap<String, UltraPrisonModule> modules;

	@Getter
	private static UltraPrisonCore instance;
	private Database pluginDatabase;
	private Economy economy;
	private FileManager fileManager;

	private UltraPrisonTokens tokens;
	private UltraPrisonGems gems;
	private UltraPrisonRanks ranks;
	private UltraPrisonMultipliers multipliers;
	private UltraPrisonEnchants enchants;
	private UltraPrisonAutoSell autoSell;
	private UltraPrisonAutoMiner autoMiner;
	private UltraPrisonPickaxeLevels pickaxeLevels;
	private UltraPrisonGangs gangs;
	private UltraPrisonMines mines;

	private NMSProvider nmsProvider;

	private List<Material> supportedPickaxes;

	private JetsPrisonMinesAPI jetsPrisonMinesAPI;

	@Getter
	private boolean ultraBackpacksEnabled;

	@Override
	protected void enable() {

		instance = this;

		this.fileManager = new FileManager(this);
		this.fileManager.getConfig("config.yml").copyDefaults(true).save();
		DEBUG_MODE = this.getConfig().getBoolean("debug-mode", false);

		if (!loadNMSProvider()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!this.initDatabase()) {
			return;
		}

		if (!this.setupEconomy()) {
			this.getLogger().warning("Economy provider for Vault not found! Economy provider is strictly required. Disabling plugin...");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			this.getLogger().info("Economy provider for Vault found - " + this.getEconomy().getName());
		}

		this.initModules();
		this.pluginDatabase.createTables();
		this.loadModules();
		this.initVariables();

		this.registerPlaceholders();
		this.registerJetsPrisonMines();

		this.registerMainEvents();
		this.registerMainCommand();
		this.startMetrics();

		SkullUtils.init();
	}

	private void initVariables() {
		this.supportedPickaxes = this.getConfig().getStringList("supported-pickaxes").stream().map(CompMaterial::fromString).map(CompMaterial::getMaterial).collect(Collectors.toList());

		for (Material m : this.supportedPickaxes) {
			this.getLogger().info("Added support for " + m);
		}

		this.ultraBackpacksEnabled = this.getServer().getPluginManager().isPluginEnabled("UltraBackpacks");
	}

	private void loadModules() {
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

		if (this.getConfig().getBoolean("modules.autosell")) {
			if (this.ultraBackpacksEnabled) {
				this.getLogger().info("Module AutoSell will not be loaded because selling system is handled by UltraBackpacks.");
			} else {
				this.loadModule(autoSell);
			}
		}

		if (this.getConfig().getBoolean("modules.mines")) {
			this.loadModule(mines);
		}

		if (this.getConfig().getBoolean("modules.enchants")) {
			this.loadModule(enchants);
		}
		if (this.getConfig().getBoolean("modules.autominer")) {
			this.loadModule(autoMiner);
		}
		if (this.getConfig().getBoolean("modules.gangs")) {
			this.loadModule(gangs);
		}
		if (this.getConfig().getBoolean("modules.pickaxe_levels")) {
			if (!this.isModuleEnabled("Enchants")) {
				this.getLogger().warning(Text.colorize("UltraPrisonCore - Module 'Pickaxe Levels' requires to have enchants module enabled."));
			} else {
				this.loadModule(pickaxeLevels);
			}
		}
	}

	private boolean initDatabase() {
		try {
			String databaseType = this.getConfig().getString("database_type");

			if (databaseType.equalsIgnoreCase("sqlite")) {
				this.pluginDatabase = new SQLiteDatabase(this);
			} else if (databaseType.equalsIgnoreCase("mysql")) {
				this.pluginDatabase = new MySQLDatabase(this, DatabaseCredentials.fromConfig(this.getConfig()));
			} else {
				this.getLogger().warning(String.format("Error! Unknown database type: %s. Disabling plugin.", databaseType));
				this.getServer().getPluginManager().disablePlugin(this);
				return false;
			}

		} catch (Exception e) {
			this.getLogger().warning("Could not maintain Database Connection. Disabling plugin.");
			e.printStackTrace();
			this.getServer().getPluginManager().disablePlugin(this);
			return false;
		}
		return true;
	}

	private void initModules() {
		this.modules = new LinkedHashMap<>();

		this.tokens = new UltraPrisonTokens(this);
		this.gems = new UltraPrisonGems(this);
		this.ranks = new UltraPrisonRanks(this);
		this.multipliers = new UltraPrisonMultipliers(this);
		this.enchants = new UltraPrisonEnchants(this);
		this.autoSell = new UltraPrisonAutoSell(this);
		this.autoMiner = new UltraPrisonAutoMiner(this);
		this.pickaxeLevels = new UltraPrisonPickaxeLevels(this);
		this.gangs = new UltraPrisonGangs(this);
		this.mines = new UltraPrisonMines(this);

		this.modules.put(this.tokens.getName().toLowerCase(), this.tokens);
		this.modules.put(this.gems.getName().toLowerCase(), this.gems);
		this.modules.put(this.ranks.getName().toLowerCase(), this.ranks);
		this.modules.put(this.multipliers.getName().toLowerCase(), this.multipliers);
		this.modules.put(this.enchants.getName().toLowerCase(), this.enchants);
		this.modules.put(this.autoSell.getName().toLowerCase(), this.autoSell);
		this.modules.put(this.autoMiner.getName().toLowerCase(), this.autoMiner);
		this.modules.put(this.pickaxeLevels.getName().toLowerCase(), this.pickaxeLevels);
		this.modules.put(this.gangs.getName().toLowerCase(), this.gangs);
		this.modules.put(this.mines.getName().toLowerCase(), this.mines);
	}

	private void registerMainEvents() {
		//Updating of mapping table
		Events.subscribe(PlayerJoinEvent.class, EventPriority.LOW)
				.handler(e -> {
					this.pluginDatabase.updatePlayerNickname(e.getPlayer());
				}).bindWith(this);
	}

	private void startMetrics() {
		new Metrics(this, METRICS_SERVICE_ID);
	}

	private void loadModule(UltraPrisonModule module) {
		module.enable();
		this.getLogger().info(Text.colorize(String.format("UltraPrisonCore - Module %s loaded.", module.getName())));
	}

	//Always unload via iterator!
	private void unloadModule(UltraPrisonModule module) {
		module.disable();
		this.getLogger().info(Text.colorize(String.format("UltraPrisonCore - Module %s unloaded.", module.getName())));
	}

	public void debug(String msg, UltraPrisonModule module) {
		if (!DEBUG_MODE) {
			return;
		}
		if (module != null) {
			this.getLogger().info(String.format("[%s] %s", module.getName(), Text.colorize(msg)));
		} else {
			this.getLogger().info(Text.colorize(msg));
		}
	}

	private void reloadModule(UltraPrisonModule module) {
		module.reload();
		this.getLogger().info(Text.colorize(String.format("UltraPrisonCore - Module %s reloaded.", module.getName())));
	}

	private void registerMainCommand() {

		List<String> commandAliases = this.getConfig().getStringList("main-command-aliases");
		String[] commandAliasesArray = commandAliases.toArray(new String[commandAliases.size()]);

		Commands.create()
				.handler(c -> {
					if (c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("reload") && c.sender().hasPermission("ultraprisoncore.admin")) {
						this.reload(c.sender());
					} else if (((c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("help")) || c.args().size() == 0) && c.sender() instanceof Player) {
						new HelpGui((Player) c.sender()).open();
					} else if ((c.rawArg(0).equalsIgnoreCase("cleardb") || c.rawArg(0).equalsIgnoreCase("resetdb") || c.rawArg(0).equalsIgnoreCase("cleardata")) && c.sender().hasPermission("ultraprisoncore.admin")) {
						UltraPrisonModule module = null;

						if (c.args().size() == 2) {
							module = this.getModuleByName(c.rawArg(1));
							if (module == null) {
								PlayerUtils.sendMessage(c.sender(), Text.colorize("&cUltraPrisonCore - Unable to get module named " + c.rawArg(0)));
								return;
							}
						}

						if (c.sender() instanceof Player) {
							new ClearDBGui(this.pluginDatabase, (Player) c.sender(), module).open();
						} else {
							if (this.pluginDatabase.resetAllData()) {
								PlayerUtils.sendMessage(c.sender(), Text.colorize("&aUltraPrisonCore - All Modules Data have been reset."));
							} else {
								PlayerUtils.sendMessage(c.sender(), Text.colorize("&cUltraPrisonCore - Something went wrong during reseting data. Please check console."));
							}
						}
					} else if (c.args().size() == 1 && (c.rawArg(0).equalsIgnoreCase("version") || c.rawArg(0).equalsIgnoreCase("v")) && c.sender().hasPermission("ultraprisoncore.admin")) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&7This server is running &f" + this.getDescription().getFullName()));
					} else if (c.args().size() == 1 && c.rawArg(0).equalsIgnoreCase("debug") && c.sender().hasPermission("ultraprisoncore.admin")) {
						DEBUG_MODE = !DEBUG_MODE;
						this.getConfig().set("debug-mode", DEBUG_MODE);
						this.saveConfig();
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&7Debug Mode: " + (DEBUG_MODE ? "&aON" : "&cOFF")));
					} else if (c.args().size() == 2 && c.rawArg(0).equalsIgnoreCase("reload") && c.sender().hasPermission("ultraprisoncore.admin")) {
						UltraPrisonModule module = this.getModuleByName(c.rawArg(1));
						if (module == null) {
							PlayerUtils.sendMessage(c.sender(), Text.colorize("&cModule " + c.rawArg(1) + " is not loaded."));
							return;
						}
						this.reloadModule(module);
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&aModule " + c.rawArg(1) + " reloaded."));

					}
				}).registerAndBind(this, commandAliasesArray);
	}

	private void reload(CommandSender sender) {
		for (UltraPrisonModule module : this.modules.values()) {
			this.reloadModule(module);
		}
		PlayerUtils.sendMessage(sender, Text.colorize("&aUltraPrisonCore - Reloaded."));
	}


	@Override
	protected void disable() {

		Iterator<UltraPrisonModule> it = this.modules.values().iterator();

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


	public boolean isModuleEnabled(String moduleName) {
		return this.modules.containsKey(moduleName.toLowerCase());
	}

	private UltraPrisonModule getModuleByName(String name) {
		if (!this.isModuleEnabled(name)) {
			return null;
		}
		return this.modules.get(name.toLowerCase());
	}

	private void registerPlaceholders() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new UltraPrisonPAPIPlaceholder(this).register();
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

	public boolean isPickaxeSupported(Material m) {
		return this.supportedPickaxes.contains(m);
	}

	public boolean isPickaxeSupported(ItemStack item) {
		return isPickaxeSupported(item.getType());
	}

	private boolean loadNMSProvider() {
		try {
			String packageName = NMSProvider.class.getPackage().getName();
			String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			this.nmsProvider = (NMSProvider) Class.forName(packageName + ".NMSProvider_" + internalsName).newInstance();
			this.getLogger().info("NMSProvider loaded for version " + internalsName);
			return true;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException exception) {
			this.getLogger().warning("NMSProvider could not find a valid implementation for this server version.");
			return false;
		}
	}

	public Collection<UltraPrisonModule> getModules() {
		return this.modules.values();
	}
}
