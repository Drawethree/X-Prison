package dev.drawethree.xprison.enchants;

import dev.drawethree.xprison.XPrisonLite;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.enchants.command.*;
import dev.drawethree.xprison.enchants.config.EnchantsConfig;
import dev.drawethree.xprison.enchants.gui.DisenchantGUI;
import dev.drawethree.xprison.enchants.gui.EnchantGUI;
import dev.drawethree.xprison.enchants.listener.EnchantsListener;
import dev.drawethree.xprison.enchants.loader.EnchantLoader;
import dev.drawethree.xprison.enchants.managers.CooldownManager;
import dev.drawethree.xprison.enchants.managers.EnchantsManager;
import dev.drawethree.xprison.enchants.managers.RespawnManager;
import dev.drawethree.xprison.enchants.repo.EnchantsRepository;
import dev.drawethree.xprison.mines.XPrisonMines;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import lombok.Getter;
import me.lucko.helper.utils.Players;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.*;

public final class XPrisonEnchants extends XPrisonModuleBase {


	public static final String MODULE_NAME = "Enchants";

	@Getter
	private static XPrisonEnchants instance;

	@Getter
	private EnchantsManager enchantsManager;

	@Getter
	private CooldownManager cooldownManager;

	@Getter
	private RespawnManager respawnManager;

	@Getter
	private EnchantsConfig enchantsConfig;

	@Getter
	private EnchantsListener enchantsListener;

	@Getter
	private EnchantsRepository enchantsRepository;

	@Getter
	private EnchantLoader enchantLoader;

	public XPrisonEnchants(XPrisonLite core) {
		super(core);
		instance = this;
	}

	@Override
	public void reload() {
		super.reload();
		this.enchantsConfig.reload();
		this.enchantsRepository.reload();

		EnchantGUI.init();
		DisenchantGUI.init();

	}

	@Override
	public void enable() {
		super.enable();
		copyDefaultEnchants();

		this.enchantsConfig = new EnchantsConfig(this);
		this.enchantsConfig.load();

		this.cooldownManager = new CooldownManager();
		this.respawnManager = new RespawnManager();

		this.enchantsManager = new EnchantsManager(this);
		this.enchantsManager.enable();

		this.enchantsListener = new EnchantsListener(this);
		this.enchantsListener.register();

		this.registerCommands();

		this.enchantsRepository = new EnchantsRepository(this);
		this.enchantLoader = new EnchantLoader(enchantsRepository);
		this.enchantLoader.load();

		EnchantGUI.init();
		DisenchantGUI.init();

		this.enabled = true;
	}

	private void copyDefaultEnchants() {
		File enchantsFolder = new File(this.core.getDataFolder(), "enchants");
		if (!enchantsFolder.exists()) {
			enchantsFolder.mkdirs();
		}

		String[] enchantFiles = {
				"autosell.json", "blessing.json", "charity.json",
				"efficiency.json", "fly.json", "fortune.json", "haste.json", "jumpboost.json",
				"layer.json", "nightvision.json",
				"salary.json", "speed.json",
				"tokenator.json", "unbreaking.json"
		};

		for (String fileName : enchantFiles) {
			File outFile = new File(enchantsFolder, fileName);
			if (!outFile.exists()) {
				try (InputStream in = XPrisonLite.getInstance().getResource( "enchants/" + fileName)) {
					if (in != null) {
						Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						info("Copied default " + fileName);
					}
				} catch (IOException e) {
					error("Failed to copy " + fileName + ": " + e.getMessage());
				}
			}
		}
	}


	private void registerCommands() {
		DisenchantCommand disenchantCommand = new DisenchantCommand(this);
		disenchantCommand.register();

		EnchantMenuCommand enchantMenuCommand = new EnchantMenuCommand(this);
		enchantMenuCommand.register();

		GiveFirstJoinPickaxeCommand giveFirstJoinPickaxeCommand = new GiveFirstJoinPickaxeCommand(this);
		giveFirstJoinPickaxeCommand.register();
	}


	@Override
	public void disable() {
		super.disable();
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

	public boolean isAutoSellModuleEnabled() {
		return this.core.isModuleEnabled(XPrisonAutoSell.MODULE_NAME);
	}

	public boolean isMultipliersModuleEnabled() {
		return this.core.isModuleEnabled(XPrisonMultipliers.MODULE_NAME);
	}

	public boolean isMinesModuleEnabled() {
		return this.core.isModuleEnabled(XPrisonMines.MODULE_NAME);
	}

}
