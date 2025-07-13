package dev.drawethree.xprison;

import com.cryptomorin.xseries.XMaterial;
import com.github.lalyos.jfiglet.FigletFont;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.blocks.XPrisonBlocks;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.core.XPrisonCoreListener;
import dev.drawethree.xprison.core.XPrisonMainCommand;
import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.impl.MySQLDatabase;
import dev.drawethree.xprison.database.impl.SQLiteDatabase;
import dev.drawethree.xprison.database.model.ConnectionProperties;
import dev.drawethree.xprison.database.model.DatabaseCredentials;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
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

import static dev.drawethree.xprison.utils.Constants.DISCORD_LINK;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.*;

@Getter
public final class XPrisonLite extends ExtendedJavaPlugin {

	private static XPrisonLite instance;

	private boolean debugMode;
	private boolean useMetrics;
	private Map<String, XPrisonModuleBase> modules;
	private SQLDatabase pluginDatabase;
	private Economy economy;
	private FileManager fileManager;
	private XPrisonBlocks blocks;
	private XPrisonTokens tokens;
	private XPrisonRanks ranks;
	private XPrisonMultipliers multipliers;
	private XPrisonEnchants enchants;
	private XPrisonAutoSell autoSell;
	private XPrisonMines mines;
	private List<Material> supportedPickaxes;

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

	private void printOnEnableMessage() {
		try {
			info("\n\n" + FigletFont.convertOneLine("X-PRISON LITE"));
			info(this.getDescription().getVersion());
			info("&fBy: &e" + this.getDescription().getAuthors());
			info("&fWebsite: &e" + this.getDescription().getWebsite());
			info("&fDiscord Support: &e" + DISCORD_LINK);
			info("&fPremium (Paid) Version: &ehttps://www.spigotmc.org/resources/86845/");
		} catch (IOException ignored) {
		}
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

		if (this.getConfig().getBoolean("modules.tokens")) {
			this.loadModule(tokens);
		}

		if (this.getConfig().getBoolean("modules.ranks")) {
			this.loadModule(ranks);
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
		this.ranks = new XPrisonRanks(this);
		this.multipliers = new XPrisonMultipliers(this);
		this.enchants = new XPrisonEnchants(this);
		this.autoSell = new XPrisonAutoSell(this);
		this.mines = new XPrisonMines(this);

		this.modules.put(this.blocks.getName().toLowerCase(), this.blocks);
		this.modules.put(this.tokens.getName().toLowerCase(), this.tokens);
		this.modules.put(this.ranks.getName().toLowerCase(), this.ranks);
		this.modules.put(this.multipliers.getName().toLowerCase(), this.multipliers);
		this.modules.put(this.enchants.getName().toLowerCase(), this.enchants);
		this.modules.put(this.autoSell.getName().toLowerCase(), this.autoSell);
		this.modules.put(this.mines.getName().toLowerCase(), this.mines);
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

	public void debug(String msg, XPrisonModuleBase module) {
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

	public static XPrisonLite getInstance() {
		return instance;
	}

	public WorldGuardWrapper getWorldGuardWrapper() {
		return WorldGuardWrapper.getInstance();
	}

	public XPrisonModuleBase getModuleByName(String name) {
		return modules.get(name);
	}
}
