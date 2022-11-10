package dev.drawethree.ultraprisoncore.tokens;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPI;
import dev.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPIImpl;
import dev.drawethree.ultraprisoncore.tokens.config.BlockRewardsConfig;
import dev.drawethree.ultraprisoncore.tokens.config.TokensConfig;
import dev.drawethree.ultraprisoncore.tokens.listener.TokensListener;
import dev.drawethree.ultraprisoncore.tokens.managers.CommandManager;
import dev.drawethree.ultraprisoncore.tokens.managers.TokensManager;
import dev.drawethree.ultraprisoncore.tokens.repo.BlocksRepository;
import dev.drawethree.ultraprisoncore.tokens.repo.TokensRepository;
import dev.drawethree.ultraprisoncore.tokens.repo.impl.BlocksRepositoryImpl;
import dev.drawethree.ultraprisoncore.tokens.repo.impl.TokensRepositoryImpl;
import dev.drawethree.ultraprisoncore.tokens.service.BlocksService;
import dev.drawethree.ultraprisoncore.tokens.service.TokensService;
import dev.drawethree.ultraprisoncore.tokens.service.impl.BlocksServiceImpl;
import dev.drawethree.ultraprisoncore.tokens.service.impl.TokensServiceImpl;
import dev.drawethree.ultraprisoncore.tokens.task.SavePlayerDataTask;
import lombok.Getter;

public final class UltraPrisonTokens implements UltraPrisonModule {

	public static final String MODULE_NAME = "Tokens";

	@Getter
	private static UltraPrisonTokens instance;

	@Getter
	private BlockRewardsConfig blockRewardsConfig;

	@Getter
	private TokensConfig tokensConfig;

	@Getter
	private UltraPrisonTokensAPI api;

	@Getter
	private TokensManager tokensManager;

	@Getter
	private CommandManager commandManager;

	@Getter
	private TokensRepository tokensRepository;

	@Getter
	private TokensService tokensService;

	@Getter
	private BlocksRepository blocksRepository;

	@Getter
	private BlocksService blocksService;

	@Getter
	private final UltraPrisonCore core;

	private SavePlayerDataTask savePlayerDataTask;

	private boolean enabled;


	public UltraPrisonTokens(UltraPrisonCore prisonCore) {
		instance = this;
		this.core = prisonCore;
	}


	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.tokensConfig.reload();
		this.blockRewardsConfig.reload();
		this.tokensManager.reload();
		this.commandManager.reload();
	}


	@Override
	public void enable() {

		this.tokensConfig = new TokensConfig(this);
		this.blockRewardsConfig = new BlockRewardsConfig(this);

		this.tokensConfig.load();
		this.blockRewardsConfig.load();

		this.tokensRepository = new TokensRepositoryImpl(this.core.getPluginDatabase());
		this.tokensRepository.createTables();

		this.tokensService = new TokensServiceImpl(this.tokensRepository);

		this.blocksRepository = new BlocksRepositoryImpl(this.core.getPluginDatabase());
		this.blocksRepository.createTables();

		this.blocksService = new BlocksServiceImpl(this.blocksRepository);

		this.tokensManager = new TokensManager(this);
		this.tokensManager.enable();

		this.commandManager = new CommandManager(this);
		this.commandManager.enable();

		this.savePlayerDataTask = new SavePlayerDataTask(this);
		this.savePlayerDataTask.start();

		this.registerListeners();

		this.commandManager.enable();

		this.api = new UltraPrisonTokensAPIImpl(this.tokensManager);

		this.enabled = true;
	}

	private void registerListeners() {
		new TokensListener(this).subscribeToEvents();
	}


	@Override
	public void disable() {
		this.tokensManager.disable();

		this.savePlayerDataTask.stop();

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
		this.tokensRepository.clearTableData();
		this.blocksRepository.clearTableData();
		return true;
	}

}
