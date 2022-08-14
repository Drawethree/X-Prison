package dev.drawethree.ultraprisoncore.enchants;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPI;
import dev.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPIImpl;
import dev.drawethree.ultraprisoncore.enchants.command.*;
import dev.drawethree.ultraprisoncore.enchants.config.EnchantsConfig;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.enchants.gui.DisenchantGUI;
import dev.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import dev.drawethree.ultraprisoncore.enchants.listener.EnchantsListener;
import dev.drawethree.ultraprisoncore.enchants.managers.CooldownManager;
import dev.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import dev.drawethree.ultraprisoncore.enchants.managers.RespawnManager;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.utils.Constants;
import lombok.Getter;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

public final class UltraPrisonEnchants implements UltraPrisonModule {


	public static final String MODULE_NAME = "Enchants";

	@Getter
	private static UltraPrisonEnchants instance;

	@Getter
	private UltraPrisonEnchantsAPI api;

	@Getter
	private EnchantsManager enchantsManager;

	@Getter
	private CooldownManager cooldownManager;

	@Getter
	private RespawnManager respawnManager;

	@Getter
	private EnchantsConfig enchantsConfig;

	@Getter
	private final UltraPrisonCore core;

	@Getter
	private IWrappedFlag<WrappedState> enchantsWGFlag;

	private boolean enabled;

	public UltraPrisonEnchants(UltraPrisonCore core) {
		instance = this;
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {

		this.enchantsConfig.reload();

		EnchantGUI.init();
		DisenchantGUI.init();

		UltraPrisonEnchantment.reloadAll();
	}

	@Override
	public void enable() {

		this.enchantsConfig = new EnchantsConfig(this);
		this.enchantsConfig.load();

		this.cooldownManager = new CooldownManager(this);
		this.respawnManager = new RespawnManager(this);

		this.enchantsManager = new EnchantsManager(this);
		this.enchantsManager.enable();

		this.api = new UltraPrisonEnchantsAPIImpl(enchantsManager);

		EnchantsListener listener = new EnchantsListener(this);
		listener.register();

		this.registerCommands();

		EnchantGUI.init();
		DisenchantGUI.init();

		UltraPrisonEnchantment.loadDefaultEnchantments();

		this.enchantsWGFlag = this.core.getWorldGuardWrapper().getFlag(Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.class).orElseGet(null);

		this.enabled = true;
	}


	private void registerCommands() {
		DisenchantCommand disenchantCommand = new DisenchantCommand(this);
		disenchantCommand.register();

		EnchantMenuCommand enchantMenuCommand = new EnchantMenuCommand(this);
		enchantMenuCommand.register();

		GiveFirstJoinPickaxeCommand giveFirstJoinPickaxeCommand = new GiveFirstJoinPickaxeCommand(this);
		giveFirstJoinPickaxeCommand.register();

		GivePickaxeCommand givePickaxeCommand = new GivePickaxeCommand(this);
		givePickaxeCommand.register();

		ValueCommand valueCommand = new ValueCommand(this);
		valueCommand.register();
	}


	@Override
	public void disable() {
		for (Player p : Players.all()) {
			p.closeInventory();
		}
		this.enchantsManager.disable();
		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[0];
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		return new String[0];
	}

	@Override
	public boolean isHistoryEnabled() {
		return false;
	}

	public boolean isAutoSellModuleEnabled() {
		return this.core.isModuleEnabled(UltraPrisonAutoSell.MODULE_NAME);
	}

	public boolean isMultipliersModuleEnabled() {
		return this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
	}

	public boolean isMinesModuleEnabled() {
		return this.core.isModuleEnabled(UltraPrisonMines.MODULE_NAME);
	}

}
