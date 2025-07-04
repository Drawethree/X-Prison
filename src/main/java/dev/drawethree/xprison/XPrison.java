package dev.drawethree.xprison;

import com.cryptomorin.xseries.XMaterial;
import com.github.lalyos.jfiglet.FigletFont;
import dev.drawethree.xprison.api.XPrisonAPI;
import dev.drawethree.xprison.api.XPrisonAPIImpl;
import dev.drawethree.xprison.api.XPrisonModule;
import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.blocks.XPrisonBlocks;
import dev.drawethree.xprison.bombs.XPrisonBombs;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.core.XPrisonCoreListener;
import dev.drawethree.xprison.core.XPrisonMainCommand;
import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.impl.MySQLDatabase;
import dev.drawethree.xprison.database.impl.SQLiteDatabase;
import dev.drawethree.xprison.database.model.ConnectionProperties;
import dev.drawethree.xprison.database.model.DatabaseCredentials;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.history.XPrisonHistory;
import dev.drawethree.xprison.migrator.ItemMigrator;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import dev.drawethree.xprison.nicknames.repo.NicknameRepository;
import dev.drawethree.xprison.nicknames.service.NicknameService;
import dev.drawethree.xprison.nicknames.service.impl.NicknameServiceImpl;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.placeholders.XPrisonMVdWPlaceholder;
import dev.drawethree.xprison.placeholders.XPrisonPAPIPlaceholder;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.log.XPrisonLogger;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.*;

@Getter
public final class XPrison extends ExtendedJavaPlugin {

	private static XPrison instance;

	private boolean debugMode;
	private boolean useMetrics;
	private Map<String, XPrisonModuleBase> modules;
	private SQLDatabase pluginDatabase;
	private Economy economy;
	private FileManager fileManager;
	private XPrisonBlocks blocks;
	private XPrisonTokens tokens;
	private XPrisonGems gems;
	private XPrisonRanks ranks;
	private XPrisonPrestiges prestiges;
	private XPrisonMultipliers multipliers;
	private XPrisonEnchants enchants;
	private XPrisonAutoSell autoSell;
	private XPrisonAutoMiner autoMiner;
	private XPrisonPickaxeLevels pickaxeLevels;
	private XPrisonGangs gangs;
	private XPrisonMines mines;
	private XPrisonBombs bombs;
	private XPrisonHistory history;
	private ItemMigrator itemMigrator;
	private List<Material> supportedPickaxes;
	private NicknameService nicknameService;

	@Override
	protected void load() {
		instance = this;
		XPrisonLogger.setLogger(this.getLogger());
		registerWGFlag();
	}

	@Override
	protected void enable() {
		this.printOnEnableMessage();
		this.modules = new LinkedHashMap<>();
		this.fileManager = new FileManager(this);
		this.fileManager.getConfig("config.yml").copyDefaults(true).save();
		this.debugMode = this.getConfig().getBoolean("debug-mode", false);
		this.useMetrics = this.getConfig().getBoolean("enable-metrics", false);

		if (!this.initDatabase()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if (!this.setupEconomy()) {
			warning("Economy provider for Vault not found! Economy provider is strictly required. Disabling plugin...");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			info("&fEconomy provider for Vault found - &e" + this.getEconomy().getName());
		}

		this.initVariables();
		this.initModules();
		this.loadModules();
		this.initApi();

		this.itemMigrator = new ItemMigrator(this);
		this.itemMigrator.reload();

		this.initNicknameService();

		this.registerPlaceholders();

		this.registerMainCommand();
		this.registerCoreListener();

		this.startMetricsIfEnabled();

		SkullUtils.init();
	}

	private void registerMainCommand() {
		new XPrisonMainCommand(this).register();
	}

	private void registerCoreListener() {
		new XPrisonCoreListener(this).subscribeToEvents();
	}

	private void initApi() {
		XPrisonAPI.setInstance(new XPrisonAPIImpl(this));
	}

	private void printOnEnableMessage() {
		try {
			info("\n\n" + FigletFont.convertOneLine("X-PRISON"));
			info(this.getDescription().getVersion());
			info("&fBy: &e" + this.getDescription().getAuthors());
			info("&fWebsite: &e" + this.getDescription().getWebsite());
			info("&fDiscord Support: &e" + Constants.DISCORD_LINK);
		} catch (IOException ignored) {
		}
	}

	private void initNicknameService() {
		NicknameRepository nicknameRepository = new NicknameRepository(this.getPluginDatabase());
		nicknameRepository.createTables();
		this.nicknameService = new NicknameServiceImpl(nicknameRepository);
	}

	private void initVariables() {
		this.supportedPickaxes = this.getConfig().getStringList("supported-pickaxes").stream().map(XMaterial::valueOf).map(XMaterial::get).collect(Collectors.toList());

		for (Material m : this.supportedPickaxes) {
			info("&fAdded support for pickaxe: &e" + m);
		}
	}

	private void loadModules() {

		if (this.getConfig().getBoolean("modules.blocks")) {
			this.loadModule(blocks);
		}

		if (this.getConfig().getBoolean("modules.bombs")) {
			this.loadModule(bombs);
		}

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
				info("&fModule &eAutoSell &fwill &cNOT BE LOADED &fbecause selling system is handled by &eUltraBackpacks&f.");
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
				warning("Module 'Pickaxe Levels' requires to have enchants module enabled.");
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
			ConnectionProperties connectionProperties = ConnectionProperties.fromConfig(this.getConfig());

			if ("sqlite".equalsIgnoreCase(databaseType)) {
				this.pluginDatabase = new SQLiteDatabase(this, connectionProperties);
				info("&fUsing &aSQLite DB.");
			} else if ("mysql".equalsIgnoreCase(databaseType)) {
				DatabaseCredentials credentials = DatabaseCredentials.fromConfig(this.getConfig());
				this.pluginDatabase = new MySQLDatabase(this, credentials, connectionProperties);
				info("&fUsing &aMySQL DB.");
			} else {
				warning(String.format("Error! Unknown database type: %s. Disabling plugin.", databaseType));
				this.getServer().getPluginManager().disablePlugin(this);
				return false;
			}

			this.pluginDatabase.connect();
		} catch (Exception e) {
			error("Could not maintain Database Connection. Disabling plugin.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void initModules() {
		this.blocks = new XPrisonBlocks(this);
		this.tokens = new XPrisonTokens(this);
		this.gems = new XPrisonGems(this);
		this.ranks = new XPrisonRanks(this);
		this.prestiges = new XPrisonPrestiges(this);
		this.multipliers = new XPrisonMultipliers(this);
		this.enchants = new XPrisonEnchants(this);
		this.autoSell = new XPrisonAutoSell(this);
		this.autoMiner = new XPrisonAutoMiner(this);
		this.pickaxeLevels = new XPrisonPickaxeLevels(this);
		this.gangs = new XPrisonGangs(this);
		this.mines = new XPrisonMines(this);
		this.bombs = new XPrisonBombs(this);
		this.history = new XPrisonHistory(this);

		this.modules.put(this.blocks.getName().toLowerCase(), this.blocks);
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
		this.modules.put(this.bombs.getName().toLowerCase(), this.bombs);
		this.modules.put(this.history.getName().toLowerCase(), this.history);
	}

	private void startMetricsIfEnabled() {
		if (useMetrics) {
			new Metrics(this, Constants.METRICS_SERVICE_ID);
		}
	}

	private void loadModule(XPrisonModuleBase module) {
		if (module.isEnabled()) {
			return;
		}
		module.enable();
		info(String.format("&aModule &e%s &aloaded.", module.getName()));
	}

	//Always unload via iterator!
	private void unloadModule(XPrisonModuleBase module) {
		if (!module.isEnabled()) {
			return;
		}
		module.disable();
		info(String.format("&cModule &e%s &cunloaded.", module.getName()));
	}

	public void debug(String msg, XPrisonModule module) {
		if (!this.debugMode) {
			return;
		}
		if (module != null) {
			info(String.format("&7[&e%s&7] &f%s", module.getName(), TextUtils.applyColor(msg)));
		} else {
			info(TextUtils.applyColor(msg));
		}
	}

	public void reloadModule(XPrisonModuleBase module) {
		if (!module.isEnabled()) {
			return;
		}
		module.reload();
		info(String.format("&aModule &e%s &areloaded.", module.getName()));
	}


	@Override
	protected void disable() {

		Iterator<XPrisonModuleBase> it = this.modules.values().iterator();

		while (it.hasNext()) {
			this.unloadModule(it.next());
			it.remove();
		}

		if (this.pluginDatabase != null) {
            SQLDatabase sqlDatabase = this.pluginDatabase;
            sqlDatabase.close();
        }
	}


	public boolean isModuleEnabled(String moduleName) {
		XPrisonModuleBase module = this.modules.get(moduleName.toLowerCase());
		return module != null && module.isEnabled();
	}

	private void registerPlaceholders() {

		if (isMVdWPlaceholderAPIEnabled()) {
			new XPrisonMVdWPlaceholder(this).register();
		}

		if (isPlaceholderAPIEnabled()) {
			new XPrisonPAPIPlaceholder(this).register();
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

	public Collection<XPrisonModuleBase> getModules() {
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

	private void registerWGFlag() {

		if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
			return;
		}

		try {
			getWorldGuardWrapper().registerFlag(Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.class, WrappedState.DENY);
		} catch (IllegalStateException e) {
			// This happens during plugin reloads. Flag cannot be registered as WG was already loaded,
			// so we can safely ignore this exception.
		}
	}

	public static XPrison getInstance() {
		return instance;
	}

	public WorldGuardWrapper getWorldGuardWrapper() {
		return WorldGuardWrapper.getInstance();
	}

	public XPrisonModuleBase getModuleByName(String name) {
		return modules.get(name);
	}
}
