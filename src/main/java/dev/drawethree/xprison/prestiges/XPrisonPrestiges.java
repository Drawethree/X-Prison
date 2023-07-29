package dev.drawethree.xprison.prestiges;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.prestiges.api.XPrisonPrestigesAPI;
import dev.drawethree.xprison.prestiges.api.XPrisonPrestigesAPIImpl;
import dev.drawethree.xprison.prestiges.commands.MaxPrestigeCommand;
import dev.drawethree.xprison.prestiges.commands.PrestigeAdminCommand;
import dev.drawethree.xprison.prestiges.commands.PrestigeCommand;
import dev.drawethree.xprison.prestiges.commands.PrestigeTopCommand;
import dev.drawethree.xprison.prestiges.config.PrestigeConfig;
import dev.drawethree.xprison.prestiges.listener.PrestigeListener;
import dev.drawethree.xprison.prestiges.manager.PrestigeManager;
import dev.drawethree.xprison.prestiges.repo.PrestigeRepository;
import dev.drawethree.xprison.prestiges.repo.impl.PrestigeRepositoryImpl;
import dev.drawethree.xprison.prestiges.service.PrestigeService;
import dev.drawethree.xprison.prestiges.service.impl.PrestigeServiceImpl;
import dev.drawethree.xprison.prestiges.task.SavePlayerDataTask;
import lombok.Getter;

@Getter
public final class XPrisonPrestiges implements XPrisonModule {

	public static final String MODULE_NAME = "Prestiges";

	@Getter
	private PrestigeConfig prestigeConfig;

	private PrestigeManager prestigeManager;

	@Getter
	private XPrisonPrestigesAPI api;

	private SavePlayerDataTask savePlayerDataTask;

	@Getter
	private final XPrison core;

	@Getter
	private PrestigeRepository prestigeRepository;

	@Getter
	private PrestigeService prestigeService;

	private boolean enabled;

	public XPrisonPrestiges(XPrison core) {
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.prestigeConfig.reload();
	}

	@Override
	public void enable() {
		this.enabled = true;

		this.prestigeConfig = new PrestigeConfig(this);
		this.prestigeConfig.load();

		this.prestigeRepository = new PrestigeRepositoryImpl(this.core.getPluginDatabase());
		this.prestigeRepository.createTables();

		this.prestigeService = new PrestigeServiceImpl(this.prestigeRepository);

		this.prestigeManager = new PrestigeManager(this);
		this.prestigeManager.enable();

		this.api = new XPrisonPrestigesAPIImpl(this);

		this.savePlayerDataTask = new SavePlayerDataTask(this);
		this.savePlayerDataTask.start();

		this.registerCommands();
		this.registerListeners();
	}


	@Override
	public void disable() {
		this.savePlayerDataTask.stop();
		this.prestigeManager.disable();
		this.enabled = false;
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
		this.prestigeRepository.clearTableData();
	}

	private void registerCommands() {
		new PrestigeCommand(this).register();
		new MaxPrestigeCommand(this).register();
		new PrestigeTopCommand(this).register();
		new PrestigeAdminCommand(this).register();
	}

	private void registerListeners() {
		new PrestigeListener(this).register();
	}
}
