package dev.drawethree.ultraprisoncore.autosell;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autosell.api.UltraPrisonAutoSellAPI;
import dev.drawethree.ultraprisoncore.autosell.api.UltraPrisonAutoSellAPIImpl;
import dev.drawethree.ultraprisoncore.autosell.command.AutoSellCommand;
import dev.drawethree.ultraprisoncore.autosell.command.SellAllCommand;
import dev.drawethree.ultraprisoncore.autosell.command.SellPriceCommand;
import dev.drawethree.ultraprisoncore.autosell.config.AutoSellConfig;
import dev.drawethree.ultraprisoncore.autosell.listener.AutoSellListener;
import dev.drawethree.ultraprisoncore.autosell.manager.AutoSellManager;
import dev.drawethree.ultraprisoncore.autosell.model.AutoSellBroadcastTask;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import lombok.Getter;

public final class UltraPrisonAutoSell implements UltraPrisonModule {

	public static final String MODULE_NAME = "Auto Sell";

	@Getter
	private static UltraPrisonAutoSell instance;
	@Getter
	private final UltraPrisonCore core;
	@Getter
	private UltraPrisonAutoSellAPI api;
	@Getter
	private AutoSellConfig autoSellConfig;
	@Getter
	private AutoSellManager manager;
	@Getter
	private AutoSellBroadcastTask broadcastTask;

	private boolean enabled;

	public UltraPrisonAutoSell(UltraPrisonCore core) {
		instance = this;
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.autoSellConfig.load();
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

        this.api = new UltraPrisonAutoSellAPIImpl(this);
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
        return this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
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

}
