package dev.drawethree.ultraprisoncore.prestiges;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.prestiges.api.UltraPrisonPrestigesAPI;
import dev.drawethree.ultraprisoncore.prestiges.api.UltraPrisonPrestigesAPIImpl;
import dev.drawethree.ultraprisoncore.prestiges.commands.MaxPrestigeCommand;
import dev.drawethree.ultraprisoncore.prestiges.commands.PrestigeAdminCommand;
import dev.drawethree.ultraprisoncore.prestiges.commands.PrestigeCommand;
import dev.drawethree.ultraprisoncore.prestiges.commands.PrestigeTopCommand;
import dev.drawethree.ultraprisoncore.prestiges.config.PrestigeConfig;
import dev.drawethree.ultraprisoncore.prestiges.listener.PrestigeListener;
import dev.drawethree.ultraprisoncore.prestiges.manager.PrestigeManager;
import dev.drawethree.ultraprisoncore.prestiges.repo.PrestigeRepository;
import dev.drawethree.ultraprisoncore.prestiges.repo.impl.PrestigeRepositoryImpl;
import dev.drawethree.ultraprisoncore.prestiges.service.PrestigeService;
import dev.drawethree.ultraprisoncore.prestiges.service.impl.PrestigeServiceImpl;
import dev.drawethree.ultraprisoncore.prestiges.task.SavePlayerDataTask;
import lombok.Getter;

@Getter
public final class UltraPrisonPrestiges implements UltraPrisonModule {

	public static final String MODULE_NAME = "Prestiges";

	@Getter
	private PrestigeConfig prestigeConfig;

	private PrestigeManager prestigeManager;

	@Getter
	private UltraPrisonPrestigesAPI api;

	private SavePlayerDataTask savePlayerDataTask;

	@Getter
	private final UltraPrisonCore core;

	@Getter
	private PrestigeRepository prestigeRepository;

	@Getter
	private PrestigeService prestigeService;

	private boolean enabled;

	public UltraPrisonPrestiges(UltraPrisonCore core) {
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

		this.api = new UltraPrisonPrestigesAPIImpl(this);

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
	public boolean resetAllData() {
		this.prestigeRepository.clearTableData();
		return true;
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
