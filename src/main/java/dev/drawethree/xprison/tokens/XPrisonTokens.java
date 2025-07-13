package dev.drawethree.xprison.tokens;

import dev.drawethree.xprison.XPrisonLite;
import dev.drawethree.xprison.XPrisonModuleBase;
import dev.drawethree.xprison.interfaces.PlayerDataHolder;
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

public final class XPrisonTokens extends XPrisonModuleBase implements PlayerDataHolder {

	public static final String MODULE_NAME = "Tokens";

	@Getter
	private static XPrisonTokens instance;

	@Getter
	private TokensConfig tokensConfig;

	@Getter
	private TokensManager tokensManager;

	@Getter
	private CommandManager commandManager;

	@Getter
	private TokensRepository tokensRepository;

	@Getter
	private TokensService tokensService;

	private RepeatingTask savePlayerDataTask;

	public XPrisonTokens(XPrisonLite prisonCore) {
		super(prisonCore);
		instance = this;
	}


	@Override
	public void reload() {
		super.reload();
		this.tokensConfig.reload();
		this.tokensManager.reload();
		this.commandManager.reload();
	}


	@Override
	public void enable() {
		super.enable();
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

		this.enabled = true;
	}

	private void registerListeners() {
		new TokensListener(this).subscribeToEvents();
	}


	@Override
	public void disable() {
		super.disable();
		this.tokensManager.disable();

		this.savePlayerDataTask.stop();

		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public void resetPlayerData() {
		this.tokensRepository.clearTableData();
	}

}
