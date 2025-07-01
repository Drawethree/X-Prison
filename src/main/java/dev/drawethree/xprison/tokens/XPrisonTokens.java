package dev.drawethree.xprison.tokens;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.XPrisonModuleAbstract;
import dev.drawethree.xprison.api.tokens.XPrisonTokensAPI;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
import dev.drawethree.xprison.tokens.api.XPrisonTokensAPIImpl;
import dev.drawethree.xprison.tokens.config.TokensConfig;
import dev.drawethree.xprison.tokens.listener.TokensListener;
import dev.drawethree.xprison.tokens.managers.CommandManager;
import dev.drawethree.xprison.tokens.managers.TokensManager;
import dev.drawethree.xprison.tokens.repo.TokensRepository;
import dev.drawethree.xprison.tokens.repo.impl.TokensRepositoryImpl;
import dev.drawethree.xprison.tokens.service.TokensService;
import dev.drawethree.xprison.tokens.service.impl.TokensServiceImpl;
import dev.drawethree.xprison.utils.task.RepeatingTask;
import lombok.Getter;
import me.lucko.helper.utils.Players;

import java.util.concurrent.TimeUnit;

public final class XPrisonTokens implements XPrisonModuleAbstract, PlayerDataHolder {

	public static final String MODULE_NAME = "Tokens";

	@Getter
	private static XPrisonTokens instance;

	@Getter
	private TokensConfig tokensConfig;

	@Getter
	private XPrisonTokensAPI api;

	@Getter
	private TokensManager tokensManager;

	@Getter
	private CommandManager commandManager;

	@Getter
	private TokensRepository tokensRepository;

	@Getter
	private TokensService tokensService;

	@Getter
	private final XPrison core;

	private RepeatingTask savePlayerDataTask;

	private boolean enabled;


	public XPrisonTokens(XPrison prisonCore) {
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
		this.tokensManager.reload();
		this.commandManager.reload();
	}


	@Override
	public void enable() {

		this.tokensConfig = new TokensConfig(this);
		this.tokensConfig.load();

		this.tokensRepository = new TokensRepositoryImpl(this.core.getPluginDatabase());
		this.tokensRepository.createTables();

		this.tokensService = new TokensServiceImpl(this.tokensRepository);

		this.tokensManager = new TokensManager(this);
		this.tokensManager.enable();

		this.commandManager = new CommandManager(this);
		this.commandManager.enable();

		this.savePlayerDataTask = new RepeatingTask.Builder().
				initialDelay(30).
				initialDelayUnit(TimeUnit.SECONDS).
				interval(getTokensConfig().getSavePlayerDataInterval()).intervalUnit(TimeUnit.MINUTES).
				task(() ->getTokensManager().savePlayerData(Players.all(), false, true)).build();
		this.savePlayerDataTask.start();

		this.registerListeners();

		this.commandManager.enable();

		this.api = new XPrisonTokensAPIImpl(this.tokensManager);

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
	public void resetPlayerData() {
		this.tokensRepository.clearTableData();
	}

}
