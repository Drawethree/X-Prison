package dev.drawethree.ultraprisoncore.ranks;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.ranks.api.UltraPrisonRanksAPI;
import dev.drawethree.ultraprisoncore.ranks.api.UltraPrisonRanksAPIImpl;
import dev.drawethree.ultraprisoncore.ranks.commands.MaxRankupCommand;
import dev.drawethree.ultraprisoncore.ranks.commands.RankupCommand;
import dev.drawethree.ultraprisoncore.ranks.commands.SetRankCommand;
import dev.drawethree.ultraprisoncore.ranks.config.RanksConfig;
import dev.drawethree.ultraprisoncore.ranks.listener.RanksListener;
import dev.drawethree.ultraprisoncore.ranks.manager.RanksManager;
import lombok.Getter;

@Getter
public final class UltraPrisonRanks implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_Ranks";
	public static final String MODULE_NAME = "Ranks";

	@Getter
	private RanksConfig ranksConfig;
	@Getter
	private RanksManager ranksManager;
	@Getter
	private UltraPrisonRanksAPI api;
	@Getter
	private final UltraPrisonCore core;

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
	public String[] getTables() {
		return new String[]{TABLE_NAME};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case MYSQL:
			case SQLITE: {
				return new String[]{
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_rank int, primary key (UUID))"
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

	private void registerCommands() {
		new RankupCommand(this).register();
		new MaxRankupCommand(this).register();
		new SetRankCommand(this).register();
	}
}
