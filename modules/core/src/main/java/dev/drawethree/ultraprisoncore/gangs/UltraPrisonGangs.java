package dev.drawethree.ultraprisoncore.gangs;


import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.gangs.api.UltraPrisonGangsAPI;
import dev.drawethree.ultraprisoncore.gangs.api.UltraPrisonGangsAPIImpl;
import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.config.GangsConfig;
import dev.drawethree.ultraprisoncore.gangs.listener.GangsListener;
import dev.drawethree.ultraprisoncore.gangs.managers.GangsManager;
import dev.drawethree.ultraprisoncore.gangs.model.GangTopByValueProvider;
import dev.drawethree.ultraprisoncore.gangs.model.GangTopProvider;
import dev.drawethree.ultraprisoncore.gangs.model.GangUpdateTopTask;
import lombok.Getter;

public final class UltraPrisonGangs implements UltraPrisonModule {

	public static final String MODULE_NAME = "Gangs";
	public static final String TABLE_NAME = "UltraPrison_Gangs";
	public static final String INVITES_TABLE_NAME = "UltraPrison_Gang_Invites";

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
	public String[] getTables() {
		return new String[]{TABLE_NAME};
	}

	@Override
	public String[] getCreateTablesSQL(DatabaseType type) {
		switch (type) {
			case SQLITE:
			case MYSQL: {
				return new String[]{
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, name varchar(36) NOT NULL UNIQUE, owner varchar(36) NOT NULL, value int default 0, members text, primary key (UUID,name))",
						"CREATE TABLE IF NOT EXISTS " + INVITES_TABLE_NAME + "(uuid varchar(36) NOT NULL, gang_id varchar(36) NOT NULL, invited_by varchar(36), invited_player varchar(36) not null, invite_date datetime not null, primary key(uuid))",
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
