package dev.drawethree.ultraprisoncore.gangs;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.gangs.api.UltraPrisonGangsAPI;
import dev.drawethree.ultraprisoncore.gangs.api.UltraPrisonGangsAPIImpl;
import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.config.GangsConfig;
import dev.drawethree.ultraprisoncore.gangs.listener.GangsListener;
import dev.drawethree.ultraprisoncore.gangs.managers.GangsManager;
import dev.drawethree.ultraprisoncore.gangs.model.GangTopByValueProvider;
import dev.drawethree.ultraprisoncore.gangs.model.GangTopProvider;
import dev.drawethree.ultraprisoncore.gangs.model.GangUpdateTopTask;
import dev.drawethree.ultraprisoncore.gangs.repo.GangsRepository;
import dev.drawethree.ultraprisoncore.gangs.repo.impl.GangsRepositoryImpl;
import dev.drawethree.ultraprisoncore.gangs.service.GangsService;
import dev.drawethree.ultraprisoncore.gangs.service.impl.GangsServiceImpl;
import lombok.Getter;

public final class UltraPrisonGangs implements UltraPrisonModule {

	public static final String MODULE_NAME = "Gangs";

	@Getter
	private static UltraPrisonGangs instance;

	@Getter
	private UltraPrisonGangsAPI api;

	@Getter
	private GangsConfig config;

	@Getter
	private GangsManager gangsManager;

	@Getter
	private GangTopProvider gangTopProvider;

	@Getter
	private GangUpdateTopTask gangUpdateTopTask;

	@Getter
	private final UltraPrisonCore core;

	@Getter
	private GangsRepository gangsRepository;

	@Getter
	private GangsService gangsService;

	private boolean enabled;

	public UltraPrisonGangs(UltraPrisonCore prisonCore) {
		instance = this;
		this.core = prisonCore;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.config.reload();
	}

	@Override
	public void enable() {
		this.config = new GangsConfig(this);
		this.config.load();

		GangCommand gangCommand = new GangCommand(this);
		gangCommand.register();

		this.gangsRepository = new GangsRepositoryImpl(this.core.getPluginDatabase());
		this.gangsRepository.createTables();

		this.gangsService = new GangsServiceImpl(this.gangsRepository);

		this.gangsManager = new GangsManager(this);
		this.gangsManager.enable();

		this.gangTopProvider = new GangTopByValueProvider(this.gangsManager);

		GangsListener gangsListener = new GangsListener(this);
		gangsListener.register();

		this.gangUpdateTopTask = new GangUpdateTopTask(this, this.gangTopProvider);
		this.gangUpdateTopTask.start();


		this.api = new UltraPrisonGangsAPIImpl(this.gangsManager);

		this.enabled = true;
	}


	@Override
	public void disable() {
		this.gangsManager.disable();
		this.gangUpdateTopTask.stop();
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
		this.gangsRepository.clearTableData();
		return true;
	}
}
