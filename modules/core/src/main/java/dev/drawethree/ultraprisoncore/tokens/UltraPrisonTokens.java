package dev.drawethree.ultraprisoncore.tokens;


import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPI;
import dev.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPIImpl;
import dev.drawethree.ultraprisoncore.tokens.config.BlockRewardsConfig;
import dev.drawethree.ultraprisoncore.tokens.config.TokensConfig;
import dev.drawethree.ultraprisoncore.tokens.listener.TokensListener;
import dev.drawethree.ultraprisoncore.tokens.managers.CommandManager;
import dev.drawethree.ultraprisoncore.tokens.managers.TokensManager;
import dev.drawethree.ultraprisoncore.tokens.task.UpdateTopBlocksTask;
import dev.drawethree.ultraprisoncore.tokens.task.UpdateTopBlocksWeeklyTask;
import dev.drawethree.ultraprisoncore.tokens.task.UpdateTopTokensTask;
import lombok.Getter;

public final class UltraPrisonTokens implements UltraPrisonModule {

	public static final String TABLE_NAME_TOKENS = "UltraPrison_Tokens";
	public static final String TABLE_NAME_BLOCKS = "UltraPrison_BlocksBroken";
	public static final String TABLE_NAME_BLOCKS_WEEKLY = "UltraPrison_BlocksBrokenWeekly";
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
	private final UltraPrisonCore core;

	private UpdateTopTokensTask updateTopTokensTask;

	private UpdateTopBlocksTask updateTopBlocksTask;

	private UpdateTopBlocksWeeklyTask updateTopBlocksWeeklyTask;

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

		this.tokensManager = new TokensManager(this);
		this.tokensManager.enable();

		this.commandManager = new CommandManager(this);
		this.commandManager.enable();

		this.updateTopTokensTask = new UpdateTopTokensTask(this);
		this.updateTopTokensTask.start();

		this.updateTopBlocksTask = new UpdateTopBlocksTask(this);
		this.updateTopBlocksTask.start();

		this.updateTopBlocksWeeklyTask = new UpdateTopBlocksWeeklyTask(this);
		this.updateTopBlocksWeeklyTask.start();

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

		this.updateTopTokensTask.stop();
		this.updateTopBlocksTask.stop();
		this.updateTopBlocksWeeklyTask.stop();

		this.enabled = false;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String[] getTables() {
		return new String[]{TABLE_NAME_BLOCKS, TABLE_NAME_TOKENS, TABLE_NAME_BLOCKS_WEEKLY};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case MYSQL:
			case SQLITE: {
				return new String[]{
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TOKENS + "(UUID varchar(36) NOT NULL UNIQUE, Tokens bigint, primary key (UUID))",
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BLOCKS + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))",
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BLOCKS_WEEKLY + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))"
				};
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + type);
		}
	}

	@Override
	public boolean isHistoryEnabled() {
		return true;
	}

}
