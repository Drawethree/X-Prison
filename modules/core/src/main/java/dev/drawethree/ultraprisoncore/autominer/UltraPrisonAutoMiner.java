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
import dev.drawethree.ultraprisoncore.autominer.repo.AutominerRepository;
import dev.drawethree.ultraprisoncore.autominer.repo.impl.AutominerRepositoryImpl;
import dev.drawethree.ultraprisoncore.autominer.service.AutominerService;
import dev.drawethree.ultraprisoncore.autominer.service.impl.AutominerServiceImpl;
import lombok.Getter;

public final class UltraPrisonAutoMiner implements UltraPrisonModule {

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

	@Getter
	private AutominerService autominerService;

	@Getter
	private AutominerRepository autominerRepository;

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

		this.autominerRepository = new AutominerRepositoryImpl(this.core.getPluginDatabase());
		this.autominerRepository.createTables();
		this.autominerRepository.removeExpiredAutoMiners();

		this.autominerService = new AutominerServiceImpl(this.autominerRepository);

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
	public boolean isHistoryEnabled() {
		return true;
	}

	@Override
	public void resetPlayerData() {
		this.autominerRepository.clearTableData();
	}

	private void registerCommands() {
		AutoMinerCommand autoMinerCommand = new AutoMinerCommand(this);
		autoMinerCommand.register();

		AdminAutoMinerCommand adminAutoMinerCommand = new AdminAutoMinerCommand(this);
		adminAutoMinerCommand.register();
	}
}
