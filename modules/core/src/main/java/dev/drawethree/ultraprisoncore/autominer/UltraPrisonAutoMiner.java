package dev.drawethree.ultraprisoncore.autominer;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autominer.api.UltraPrisonAutoMinerAPI;
import dev.drawethree.ultraprisoncore.autominer.api.UltraPrisonAutoMinerAPIImpl;
import dev.drawethree.ultraprisoncore.autominer.command.AdminAutoMinerCommand;
import dev.drawethree.ultraprisoncore.autominer.command.AutoMinerCommand;
import dev.drawethree.ultraprisoncore.autominer.config.AutoMinerConfig;
import dev.drawethree.ultraprisoncore.autominer.listener.AutoMinerListener;
import dev.drawethree.ultraprisoncore.autominer.manager.AutoMinerManager;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import lombok.Getter;

public final class UltraPrisonAutoMiner implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_AutoMiner";
	public static final String MODULE_NAME = "Auto Miner";

	@Getter
	private static UltraPrisonAutoMiner instance;

	@Getter
	private final UltraPrisonCore core;

	@Getter
	private AutoMinerManager manager;

	@Getter
	private AutoMinerConfig autoMinerConfig;

	@Getter
	private UltraPrisonAutoMinerAPI api;

	private boolean enabled;

	public UltraPrisonAutoMiner(UltraPrisonCore core) {
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void enable() {
		instance = this;

		this.autoMinerConfig = new AutoMinerConfig(this);
		this.autoMinerConfig.load();

		this.manager = new AutoMinerManager(this);
		this.manager.load();

		AutoMinerListener listener = new AutoMinerListener(this);
		listener.subscribeToEvents();

		this.registerCommands();

		this.api = new UltraPrisonAutoMinerAPIImpl(this);

		this.enabled = true;
	}

	@Override
	public void disable() {
		this.manager.disable();
		this.enabled = false;
	}

	@Override
	public void reload() {
		this.autoMinerConfig.reload();
		this.manager.reload();
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case SQLITE:
			case MYSQL: {
				return new String[]{"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, time int, primary key (UUID))"};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

	private void registerCommands() {
		AutoMinerCommand autoMinerCommand = new AutoMinerCommand(this);
		autoMinerCommand.register();

		AdminAutoMinerCommand adminAutoMinerCommand = new AdminAutoMinerCommand(this);
		adminAutoMinerCommand.register();
	}
}
