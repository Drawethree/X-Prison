package dev.drawethree.xprison;

import com.github.lalyos.jfiglet.FigletFont;
import dev.drawethree.xprison.autominer.XPrisonAutoMiner;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.impl.MySQLDatabase;
import dev.drawethree.xprison.database.impl.SQLiteDatabase;
import dev.drawethree.xprison.database.model.ConnectionProperties;
import dev.drawethree.xprison.database.model.DatabaseCredentials;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.gangs.XPrisonGangs;
import dev.drawethree.xprison.gems.XPrisonGems;
import dev.drawethree.xprison.history.XPrisonHistory;
import dev.drawethree.xprison.mainmenu.MainMenu;
import dev.drawethree.xprison.mainmenu.help.HelpGui;
import dev.drawethree.xprison.migrator.ItemMigrator;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import dev.drawethree.xprison.nicknames.repo.NicknameRepository;
import dev.drawethree.xprison.nicknames.repo.impl.NicknameRepositoryImpl;
import dev.drawethree.xprison.nicknames.service.NicknameService;
import dev.drawethree.xprison.nicknames.service.impl.NicknameServiceImpl;
import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.placeholders.XPrisonMVdWPlaceholder;
import dev.drawethree.xprison.placeholders.XPrisonPAPIPlaceholder;
import dev.drawethree.xprison.prestiges.XPrisonPrestiges;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.PersistentActionBar;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public final class XPrison extends ExtendedJavaPlugin {

	private static XPrison instance;

	private boolean debugMode;
	private Map<String, XPrisonModule> modules;
	private SQLDatabase pluginDatabase;
	private Economy economy;
	private FileManager fileManager;
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
	private XPrisonHistory history;

	private ItemMigrator itemMigrator;

	private List<Material> supportedPickaxes;

	private NicknameService nicknameService;


	@Override
	protected void load() {
		instance = this;
		registerWGFlag();
	}

	@Override
	protected void enable() {

		this.printOnEnableMessage();
		this.modules = new LinkedHashMap<>();
		this.fileManager = new FileManager(this);
		this.fileManager.getConfig("config.yml").copyDefaults(true).save();
		this.debugMode = this.getConfig().getBoolean("debug-mode", false);

		if (!this.initDatabase()) {
			this.getServer().getPluginManager().disablePlugin(this);
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
		this.loadModules();

		this.itemMigrator = new ItemMigrator(this);
		this.itemMigrator.reload();

		this.initNicknameService();

		this.registerPlaceholders();

		this.registerMainEvents();
		this.registerMainCommand();

		SkullUtils.init();
	}

	private void printOnEnableMessage() {
		try {
			this.getLogger().info(FigletFont.convertOneLine("X-PRISON"));
			this.getLogger().info(this.getDescription().getVersion());
			this.getLogger().info(FigletFont.convertOneLine("X-PRISON"));
		} catch (IOException ignored) {
		}
	}

	private void initNicknameService() {
		NicknameRepository nicknameRepository = new NicknameRepositoryImpl(this.getPluginDatabase());
		nicknameRepository.createTables();
		this.nicknameService = new NicknameServiceImpl(nicknameRepository);
	}

	private void initVariables() {
		this.supportedPickaxes = this.getConfig().getStringList("supported-pickaxes").stream().map(CompMaterial::fromString).map(CompMaterial::getMaterial).collect(Collectors.toList());

		for (Material m : this.supportedPickaxes) {
			this.getLogger().info("Added support for pickaxe: " + m);
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
				this.getLogger().warning(TextUtils.applyColor("&cX-Prison - Module 'Pickaxe Levels' requires to have enchants module enabled."));
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
				this.getLogger().info("Using SQLite (local) database.");
			} else if ("mysql".equalsIgnoreCase(databaseType)) {
				DatabaseCredentials credentials = DatabaseCredentials.fromConfig(this.getConfig());
				this.pluginDatabase = new MySQLDatabase(this, credentials, connectionProperties);
				this.getLogger().info("Using MySQL (remote) database.");
			} else {
				this.getLogger().warning(String.format("Error! Unknown database type: %s. Disabling plugin.", databaseType));
				this.getServer().getPluginManager().disablePlugin(this);
				return false;
			}

			this.pluginDatabase.connect();
		} catch (Exception e) {
			this.getLogger().warning("Could not maintain Database Connection. Disabling plugin.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void initModules() {
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
		this.history = new XPrisonHistory(this);

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
					this.nicknameService.updatePlayerNickname(e.getPlayer());
					PersistentActionBar.start(e.getPlayer(), () -> this.getRanks().getRanksConfig().getMessage("actionbar-progress"), this);
				}).bindWith(this);

		Events.subscribe(PlayerQuitEvent.class, EventPriority.LOW).handler(playerQuitEvent -> PersistentActionBar.stop(playerQuitEvent.getPlayer())).bindWith(this);
	}


	private void loadModule(@NotNull XPrisonModule module) {
		if (module.isEnabled()) {
			return;
		}
		module.enable();
		this.getLogger().info(TextUtils.applyColor(String.format("&aX-Prison - Module %s loaded.", module.getName())));
	}

	//Always unload via iterator!
	private void unloadModule(@NotNull XPrisonModule module) {
		if (!module.isEnabled()) {
			return;
		}
		module.disable();
		this.getLogger().info(TextUtils.applyColor(String.format("&aX-Prison - Module %s unloaded.", module.getName())));
	}

	public void debug(String msg, XPrisonModule module) {
		if (!this.debugMode) {
			return;
		}
		if (module != null) {
			this.getLogger().info(String.format("[%s] %s", module.getName(), TextUtils.applyColor(msg)));
		} else {
			this.getLogger().info(TextUtils.applyColor(msg));
		}
	}

	public void reloadModule(@NotNull XPrisonModule module) {
		if (!module.isEnabled()) {
			return;
		}
		module.reload();
		this.getLogger().info(TextUtils.applyColor(String.format("X-Prison - Module %s reloaded.", module.getName())));
	}

	private void registerMainCommand() {

		List<String> commandAliases = this.getConfig().getStringList("main-command-aliases");
		String[] commandAliasesArray = commandAliases.toArray(new String[commandAliases.size()]);

		Commands.create()
				.assertPermission("xprison.admin")
				.handler(c -> {
                    if (c.args().isEmpty() && c.sender() instanceof Player) {
                        new MainMenu(this, (Player) c.sender()).open();
                    } else if (!c.args().isEmpty()) {
                        if ("reload".equalsIgnoreCase(c.rawArg(0))) {
                            final String name = c.args().size() >= 2 ? c.rawArg(1).trim().toLowerCase().replace("-", "") : "all";
                            switch (name) {
                                case "all":
                                case "*":
                                    getModules().forEach(this::reloadModule);
                                    getItemMigrator().reload();
                                    c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded all the plugin"));
                                    break;
                                case "migrator":
                                case "itemmigrator":
                                    getItemMigrator().reload();
                                    c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded item migrator"));
                                    break;
                                default:
                                    final XPrisonModule module = modules.get(name);
                                    if (module != null) {
                                        reloadModule(module);
                                        c.sender().sendMessage(TextUtils.applyColor("&aSuccessfully reloaded &f" + name + " &amodule"));
                                    } else {
                                        c.sender().sendMessage(TextUtils.applyColor("&cThe module &6" + c.rawArg(1) + " &cdoesn't exist"));
                                    }
                                    break;
                            }
                        } else if (c.sender() instanceof Player && "help".equalsIgnoreCase(c.rawArg(0)) || "?".equalsIgnoreCase(c.rawArg(0))) {
                            new HelpGui((Player) c.sender()).open();
                        }
                    }
				}).registerAndBind(this, commandAliasesArray);
	}

	@Override
	protected void disable() {

		Iterator<XPrisonModule> it = this.modules.values().iterator();

		while (it.hasNext()) {
			this.unloadModule(it.next());
			it.remove();
		}

		if (this.pluginDatabase != null) {
			if (this.pluginDatabase instanceof SQLDatabase) {
				SQLDatabase sqlDatabase = this.pluginDatabase;
				sqlDatabase.close();
			}
		}
	}


	public boolean isModuleEnabled(@NotNull String moduleName) {
		XPrisonModule module = this.modules.get(moduleName.toLowerCase());
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
		return true;
	}

	public boolean isPickaxeSupported(ItemStack item) {
		return item != null && this.supportedPickaxes.contains(item.getType());
	}

	@Contract(pure = true)
	public @NotNull Collection<XPrisonModule> getModules() {
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
			// This happens during plugin reloads. The Flag cannot be registered as WG was already loaded,
			// so we can safely ignore this exception.
		}
	}

	public static XPrison getInstance() {
		return instance;
	}

	public WorldGuardWrapper getWorldGuardWrapper() {
		return WorldGuardWrapper.getInstance();
	}
}
