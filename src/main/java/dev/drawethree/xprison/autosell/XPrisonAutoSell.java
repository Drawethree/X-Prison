package dev.drawethree.xprison.autosell;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.autosell.api.XPrisonAutoSellAPI;
import dev.drawethree.xprison.autosell.api.XPrisonAutoSellAPIImpl;
import dev.drawethree.xprison.autosell.command.AutoSellCommand;
import dev.drawethree.xprison.autosell.command.SellAllCommand;
import dev.drawethree.xprison.autosell.command.SellPriceCommand;
import dev.drawethree.xprison.autosell.config.AutoSellConfig;
import dev.drawethree.xprison.autosell.listener.AutoSellListener;
import dev.drawethree.xprison.autosell.manager.AutoSellManager;
import dev.drawethree.xprison.autosell.model.AutoSellBroadcastTask;
import dev.drawethree.xprison.multipliers.XPrisonMultipliers;
import lombok.Getter;

public final class XPrisonAutoSell implements XPrisonModule {

	public static final String MODULE_NAME = "Auto Sell";

	@Getter
	private static XPrisonAutoSell instance;
	@Getter
	private final XPrison core;
	@Getter
	private XPrisonAutoSellAPI api;
	@Getter
	private AutoSellConfig autoSellConfig;
	@Getter
	private AutoSellManager manager;
	@Getter
	private AutoSellBroadcastTask broadcastTask;

	private boolean enabled;

	public XPrisonAutoSell(XPrison core) {
		instance = this;
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.autoSellConfig.reload();
		this.manager.reload();
	}


    @Override
    public void enable() {
        this.autoSellConfig = new AutoSellConfig(this);
        this.autoSellConfig.load();

        this.manager = new AutoSellManager(this);
        this.manager.load();

        this.broadcastTask = new AutoSellBroadcastTask(this);
        this.broadcastTask.start();

        AutoSellListener listener = new AutoSellListener(this);
        listener.subscribeToEvents();

        this.registerCommands();

        this.api = new XPrisonAutoSellAPIImpl(this);
        this.enabled = true;
    }

    private void registerCommands() {
        SellAllCommand sellAllCommand = new SellAllCommand(this);
        sellAllCommand.register();

        AutoSellCommand autoSellCommand = new AutoSellCommand(this);
        autoSellCommand.register();

        SellPriceCommand sellPriceCommand = new SellPriceCommand(this);
        sellPriceCommand.register();
    }

	public boolean isMultipliersModuleEnabled() {
		return this.core.isModuleEnabled(XPrisonMultipliers.MODULE_NAME);
	}

	@Override
	public void disable() {
		this.broadcastTask.stop();
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

}
