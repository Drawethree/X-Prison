package dev.drawethree.xprison.gangs;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.api.gangs.XPrisonGangsAPI;
import dev.drawethree.xprison.gangs.api.XPrisonGangsAPIImpl;
import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.config.GangsConfig;
import dev.drawethree.xprison.gangs.listener.GangsListener;
import dev.drawethree.xprison.gangs.managers.GangsManager;
import dev.drawethree.xprison.gangs.model.GangTopByValueProvider;
import dev.drawethree.xprison.gangs.model.GangTopProvider;
import dev.drawethree.xprison.gangs.model.GangUpdateTopTask;
import dev.drawethree.xprison.gangs.repo.GangsRepository;
import dev.drawethree.xprison.gangs.repo.impl.GangsRepositoryImpl;
import dev.drawethree.xprison.gangs.service.GangsService;
import dev.drawethree.xprison.gangs.service.impl.GangsServiceImpl;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import lombok.Getter;

public final class XPrisonGangs extends XPrisonModuleBase implements PlayerDataHolder {

	public static final String MODULE_NAME = "Gangs";

	@Getter
	private static XPrisonGangs instance;

	@Getter
	private XPrisonGangsAPI api;

	@Getter
	private GangsConfig config;

	@Getter
	private GangsManager gangsManager;

	@Getter
	private GangTopProvider gangTopProvider;

	@Getter
	private GangUpdateTopTask gangUpdateTopTask;

	@Getter
	private GangsRepository gangsRepository;

	@Getter
	private GangsService gangsService;

	public XPrisonGangs(XPrison prisonCore) {
		super(prisonCore);
		instance = this;
	}

	@Override
	public void reload() {
		super.reload();
		this.config.reload();
	}

	@Override
	public void enable() {
		super.enable();
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


		this.api = new XPrisonGangsAPIImpl(this.gangsManager);

		this.enabled = true;
	}


	@Override
	public void disable() {
		super.disable();
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
	public void resetPlayerData() {
		this.gangsRepository.clearTableData();
	}
}
