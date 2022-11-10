package dev.drawethree.ultraprisoncore.ranks;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.ranks.api.UltraPrisonRanksAPI;
import dev.drawethree.ultraprisoncore.ranks.api.UltraPrisonRanksAPIImpl;
import dev.drawethree.ultraprisoncore.ranks.commands.MaxRankupCommand;
import dev.drawethree.ultraprisoncore.ranks.commands.RankupCommand;
import dev.drawethree.ultraprisoncore.ranks.commands.SetRankCommand;
import dev.drawethree.ultraprisoncore.ranks.config.RanksConfig;
import dev.drawethree.ultraprisoncore.ranks.listener.RanksListener;
import dev.drawethree.ultraprisoncore.ranks.manager.RanksManager;
import dev.drawethree.ultraprisoncore.ranks.repo.RanksRepository;
import dev.drawethree.ultraprisoncore.ranks.repo.impl.RanksRepositoryImpl;
import dev.drawethree.ultraprisoncore.ranks.service.RanksService;
import dev.drawethree.ultraprisoncore.ranks.service.impl.RanksServiceImpl;
import lombok.Getter;

@Getter
public final class UltraPrisonRanks implements UltraPrisonModule {

	public static final String MODULE_NAME = "Ranks";

	@Getter
	private RanksConfig ranksConfig;
	@Getter
	private RanksManager ranksManager;
	@Getter
	private UltraPrisonRanksAPI api;
	@Getter
	private final UltraPrisonCore core;

	@Getter
	private RanksRepository ranksRepository;

	@Getter
	private RanksService ranksService;

	private boolean enabled;

	public UltraPrisonRanks(UltraPrisonCore core) {
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
		this.api = new UltraPrisonRanksAPIImpl(this.ranksManager);
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
	public boolean resetAllData() {
		this.ranksRepository.clearTableData();
		return true;
	}

	private void registerCommands() {
		new RankupCommand(this).register();
		new MaxRankupCommand(this).register();
		new SetRankCommand(this).register();
	}
}
