package dev.drawethree.ultraprisoncore.enchants;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPI;
import dev.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPIImpl;
import dev.drawethree.ultraprisoncore.enchants.command.*;
import dev.drawethree.ultraprisoncore.enchants.config.EnchantsConfig;
import dev.drawethree.ultraprisoncore.enchants.gui.DisenchantGUI;
import dev.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import dev.drawethree.ultraprisoncore.enchants.listener.EnchantsListener;
import dev.drawethree.ultraprisoncore.enchants.managers.CooldownManager;
import dev.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import dev.drawethree.ultraprisoncore.enchants.managers.RespawnManager;
import dev.drawethree.ultraprisoncore.enchants.repo.EnchantsRepository;
import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import lombok.Getter;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;

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
	private EnchantsRepository enchantsRepository;

	@Getter
	private final UltraPrisonCore core;

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
		this.enchantsRepository.reload();

		EnchantGUI.init();
		DisenchantGUI.init();

	}

	@Override
	public void enable() {

		this.enchantsConfig = new EnchantsConfig(this);
		this.enchantsConfig.load();

		this.cooldownManager = new CooldownManager(this);
		this.respawnManager = new RespawnManager(this);

		this.enchantsManager = new EnchantsManager(this);
		this.enchantsManager.enable();

		EnchantsListener listener = new EnchantsListener(this);
		listener.register();

		this.registerCommands();

		this.enchantsRepository = new EnchantsRepository(this);
		this.enchantsRepository.loadDefaultEnchantments();

		EnchantGUI.init();
		DisenchantGUI.init();

		this.api = new UltraPrisonEnchantsAPIImpl(this.enchantsManager, this.enchantsRepository);


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
	public boolean isHistoryEnabled() {
		return false;
	}

	@Override
	public void resetPlayerData() {
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
