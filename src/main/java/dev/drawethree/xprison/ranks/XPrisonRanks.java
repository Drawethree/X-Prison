package dev.drawethree.xprison.ranks;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModule;
import dev.drawethree.xprison.ranks.api.XPrisonRanksAPI;
import dev.drawethree.xprison.ranks.api.XPrisonRanksAPIImpl;
import dev.drawethree.xprison.ranks.commands.MaxRankupCommand;
import dev.drawethree.xprison.ranks.commands.RankupCommand;
import dev.drawethree.xprison.ranks.commands.SetRankCommand;
import dev.drawethree.xprison.ranks.config.RanksConfig;
import dev.drawethree.xprison.ranks.listener.RanksListener;
import dev.drawethree.xprison.ranks.manager.RanksManager;
import dev.drawethree.xprison.ranks.repo.RanksRepository;
import dev.drawethree.xprison.ranks.repo.impl.RanksRepositoryImpl;
import dev.drawethree.xprison.ranks.service.RanksService;
import dev.drawethree.xprison.ranks.service.impl.RanksServiceImpl;
import lombok.Getter;

@Getter
public final class XPrisonRanks implements XPrisonModule {

	public static final String MODULE_NAME = "Ranks";

	@Getter
	private RanksConfig ranksConfig;
	@Getter
	private RanksManager ranksManager;
	@Getter
	private XPrisonRanksAPI api;
	@Getter
	private final XPrison core;

	@Getter
	private RanksRepository ranksRepository;

	@Getter
	private RanksService ranksService;

	private boolean enabled;

	public XPrisonRanks(XPrison core) {
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.ranksConfig.reload();
	}

	@Override
	public void enable() {
		this.enabled = true;
		this.ranksConfig = new RanksConfig(this);
		this.ranksConfig.load();
		this.ranksRepository = new RanksRepositoryImpl(this.core.getPluginDatabase());
		this.ranksRepository.createTables();
		this.ranksService = new RanksServiceImpl(this.ranksRepository);
		this.ranksManager = new RanksManager(this);
		this.ranksManager.enable();
		this.api = new XPrisonRanksAPIImpl(this.ranksManager);
		this.registerCommands();
		this.registerListeners();
	}

	private void registerListeners() {
		new RanksListener(this).register();
	}

	@Override
	public void disable() {
		this.ranksManager.disable();
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
		this.ranksRepository.clearTableData();
	}

	private void registerCommands() {
		new RankupCommand(this).register();
		new MaxRankupCommand(this).register();
		new SetRankCommand(this).register();
	}
}
