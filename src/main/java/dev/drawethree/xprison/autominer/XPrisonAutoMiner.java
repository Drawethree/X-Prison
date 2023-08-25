package dev.drawethree.xprison.autominer;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.autominer.api.XPrisonAutoMinerAPI;
import dev.drawethree.xprison.autominer.api.XPrisonAutoMinerAPIImpl;
import dev.drawethree.xprison.autominer.command.AdminAutoMinerCommand;
import dev.drawethree.xprison.autominer.command.AutoMinerCommand;
import dev.drawethree.xprison.autominer.config.AutoMinerConfig;
import dev.drawethree.xprison.autominer.listener.AutoMinerListener;
import dev.drawethree.xprison.autominer.manager.AutoMinerManager;
import dev.drawethree.xprison.autominer.repo.AutominerRepository;
import dev.drawethree.xprison.autominer.repo.impl.AutominerRepositoryImpl;
import dev.drawethree.xprison.autominer.service.AutominerService;
import dev.drawethree.xprison.autominer.service.impl.AutominerServiceImpl;
import lombok.Getter;

public final class XPrisonAutoMiner implements XPrisonModule {

	public static final String MODULE_NAME = "Auto Miner";

	@Getter
	private static XPrisonAutoMiner instance;

	@Getter
	private final XPrison core;

	@Getter
	private AutoMinerManager manager;

	@Getter
	private AutoMinerConfig autoMinerConfig;

	@Getter
	private XPrisonAutoMinerAPI api;

	@Getter
	private AutominerService autominerService;

	@Getter
	private AutominerRepository autominerRepository;

	private boolean enabled;

	public XPrisonAutoMiner(XPrison core) {
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

		this.api = new XPrisonAutoMinerAPIImpl(this);

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
