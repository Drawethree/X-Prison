package dev.drawethree.ultraprisoncore;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.config.FileManager;
import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.DatabaseCredentials;
import dev.drawethree.ultraprisoncore.database.SQLDatabase;
import dev.drawethree.ultraprisoncore.database.implementations.MySQLDatabase;
import dev.drawethree.ultraprisoncore.database.implementations.SQLiteDatabase;
import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.history.UltraPrisonHistory;
import dev.drawethree.ultraprisoncore.mainmenu.MainMenu;
import dev.drawethree.ultraprisoncore.mainmenu.help.HelpGui;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.nms.NMSProvider;
import dev.drawethree.ultraprisoncore.pickaxelevels.UltraPrisonPickaxeLevels;
import dev.drawethree.ultraprisoncore.placeholders.UltraPrisonMVdWPlaceholder;
import dev.drawethree.ultraprisoncore.placeholders.UltraPrisonPAPIPlaceholder;
import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import dev.drawethree.ultraprisoncore.utils.Constants;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.misc.SkullUtils;
import dev.drawethree.ultraprisoncore.utils.text.TextUtils;
import lombok.Getter;
import me.jet315.prisonmines.JetsPrisonMines;
import me.jet315.prisonmines.JetsPrisonMinesAPI;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.stream.Collectors;


@Getter
public final class UltraPrisonCore extends ExtendedJavaPlugin {

	@Getter
	private static UltraPrisonCore instance;

	private boolean debugMode;
	private Map<String, UltraPrisonModule> modules;
	private Database pluginDatabase;
	private Economy economy;
	private FileManager fileManager;

	private UltraPrisonTokens tokens;
	private UltraPrisonGems gems;
	private UltraPrisonRanks ranks;
	private UltraPrisonPrestiges prestiges;
	private UltraPrisonMultipliers multipliers;
	private UltraPrisonEnchants enchants;
	private UltraPrisonAutoSell autoSell;
	private UltraPrisonAutoMiner autoMiner;
	private UltraPrisonPickaxeLevels pickaxeLevels;
	private UltraPrisonGangs gangs;
	private UltraPrisonMines mines;
	private UltraPrisonHistory history;

	private NMSProvider nmsProvider;

	private List<Material> supportedPickaxes;

	private JetsPrisonMinesAPI jetsPrisonMinesAPI;


	@Override
	protected void enable() {

		instance = this;
		this.fileManager = new FileManager(this);
		this.fileManager.getConfig("config.yml").copyDefaults(true).save();
		this.debugMode = this.getConfig().getBoolean("debug-mode", false);

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

		this.initVariables();

		this.initModules();
		this.pluginDatabase.createTables();
		this.pluginDatabase.createIndexes();
		this.loadModules();

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

		if (this.getConfig().getBoolean("modules.prestiges")) {
			this.loadModule(prestiges);
		}

		if (this.getConfig().getBoolean("modules.multipliers")) {
			this.loadModule(multipliers);
		}

		if (this.getConfig().getBoolean("modules.autosell")) {
			if (isUltraBackpacksEnabled()) {
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
				this.getLogger().warning(TextUtils.applyColor("&cUltraPrisonCore - Module 'Pickaxe Levels' requires to have enchants module enabled."));
			} else {
				this.loadModule(pickaxeLevels);
			}
		}
		if (this.getConfig().getBoolean("modules.history")) {
			this.loadModule(history);
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
		this.prestiges = new UltraPrisonPrestiges(this);
		this.multipliers = new UltraPrisonMultipliers(this);
		this.enchants = new UltraPrisonEnchants(this);
		this.autoSell = new UltraPrisonAutoSell(this);
		this.autoMiner = new UltraPrisonAutoMiner(this);
		this.pickaxeLevels = new UltraPrisonPickaxeLevels(this);
		this.gangs = new UltraPrisonGangs(this);
		this.mines = new UltraPrisonMines(this);
		this.history = new UltraPrisonHistory(this);

		this.modules.put(this.tokens.getName().toLowerCase(), this.tokens);
		this.modules.put(this.gems.getName().toLowerCase(), this.gems);
		this.modules.put(this.ranks.getName().toLowerCase(), this.ranks);
		this.modules.put(this.prestiges.getName().toLowerCase(), this.prestiges);
		this.modules.put(this.multipliers.getName().toLowerCase(), this.multipliers);
		this.modules.put(this.enchants.getName().toLowerCase(), this.enchants);
		this.modules.put(this.autoSell.getName().toLowerCase(), this.autoSell);
		this.modules.put(this.autoMiner.getName().toLowerCase(), this.autoMiner);
		this.modules.put(this.pickaxeLevels.getName().toLowerCase(), this.pickaxeLevels);
		this.modules.put(this.gangs.getName().toLowerCase(), this.gangs);
		this.modules.put(this.mines.getName().toLowerCase(), this.mines);
		this.modules.put(this.history.getName().toLowerCase(), this.history);
	}

	private void registerMainEvents() {
		//Updating of mapping table
		Events.subscribe(PlayerJoinEvent.class, EventPriority.LOW)
				.handler(e -> {
					this.pluginDatabase.updatePlayerNickname(e.getPlayer());
				}).bindWith(this);
	}

	private void startMetrics() {
		new Metrics(this, Constants.METRICS_SERVICE_ID);
	}

	private void loadModule(UltraPrisonModule module) {
		if (module.isEnabled()) {
			return;
		}
		module.enable();
		this.getLogger().info(TextUtils.applyColor(String.format("&aUltraPrisonCore - Module %s loaded.", module.getName())));
	}

	//Always unload via iterator!
	private void unloadModule(UltraPrisonModule module) {
		if (!module.isEnabled()) {
			return;
		}
		module.disable();
		this.getLogger().info(TextUtils.applyColor(String.format("&aUltraPrisonCore - Module %s unloaded.", module.getName())));
	}

	public void debug(String msg, UltraPrisonModule module) {
		if (!this.debugMode) {
			return;
		}
		if (module != null) {
			this.getLogger().info(String.format("[%s] %s", module.getName(), TextUtils.applyColor(msg)));
		} else {
			this.getLogger().info(TextUtils.applyColor(msg));
		}
	}

	public void reloadModule(UltraPrisonModule module) {
		if (!module.isEnabled()) {
			return;
		}
		module.reload();
		this.getLogger().info(TextUtils.applyColor(String.format("UltraPrisonCore - Module %s reloaded.", module.getName())));
	}

	private void registerMainCommand() {

		List<String> commandAliases = this.getConfig().getStringList("main-command-aliases");
		String[] commandAliasesArray = commandAliases.toArray(new String[commandAliases.size()]);

		Commands.create()
				.assertPermission("ultraprisoncore.mainmenu")
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						new MainMenu(this, c.sender()).open();
					} else if (c.args().size() == 1 && "help".equalsIgnoreCase(c.rawArg(0)) || "?".equalsIgnoreCase(c.rawArg(0))) {
						new HelpGui(c.sender()).open();
					}
				}).registerAndBind(this, commandAliasesArray);
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
		UltraPrisonModule module = this.modules.get(moduleName.toLowerCase());
		return module != null && module.isEnabled();
	}

	private void registerPlaceholders() {

		if (isMVdWPlaceholderAPIEnabled()) {
			new UltraPrisonMVdWPlaceholder(this).register();
		}

		if (isPlaceholderAPIEnabled()) {
			new UltraPrisonPAPIPlaceholder(this).register();
		}
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
		return item != null && isPickaxeSupported(item.getType());
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

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean enabled) {
		this.debugMode = enabled;
		this.getConfig().set("debug-mode", debugMode);
		this.saveConfig();
	}

	public boolean isUltraBackpacksEnabled() {
		return this.getServer().getPluginManager().isPluginEnabled("UltraBackpacks");
	}

	public boolean isPlaceholderAPIEnabled() {
		return this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
	}

	public boolean isMVdWPlaceholderAPIEnabled() {
		return this.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI");
	}
}
