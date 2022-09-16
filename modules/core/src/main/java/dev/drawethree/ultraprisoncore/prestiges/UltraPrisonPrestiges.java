package dev.drawethree.ultraprisoncore.prestiges;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.prestiges.api.UltraPrisonPrestigesAPI;
import dev.drawethree.ultraprisoncore.prestiges.api.UltraPrisonPrestigesAPIImpl;
import dev.drawethree.ultraprisoncore.prestiges.commands.MaxPrestigeCommand;
import dev.drawethree.ultraprisoncore.prestiges.commands.PrestigeAdminCommand;
import dev.drawethree.ultraprisoncore.prestiges.commands.PrestigeCommand;
import dev.drawethree.ultraprisoncore.prestiges.commands.PrestigeTopCommand;
import dev.drawethree.ultraprisoncore.prestiges.config.PrestigeConfig;
import dev.drawethree.ultraprisoncore.prestiges.listener.PrestigeListener;
import dev.drawethree.ultraprisoncore.prestiges.manager.PrestigeManager;
import dev.drawethree.ultraprisoncore.prestiges.task.SavePlayerDataTask;
import lombok.Getter;

@Getter
public final class UltraPrisonPrestiges implements UltraPrisonModule {

	public static final String TABLE_NAME = "UltraPrison_Prestiges";
	public static final String MODULE_NAME = "Prestiges";

	@Getter
	private PrestigeConfig prestigeConfig;

	private PrestigeManager prestigeManager;

	@Getter
	private UltraPrisonPrestigesAPI api;

	private SavePlayerDataTask savePlayerDataTask;

	@Getter
	private final UltraPrisonCore core;

	private boolean enabled;

	public UltraPrisonPrestiges(UltraPrisonCore core) {
		this.core = core;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {
		this.prestigeConfig.reload();
	}

	@Override
	public void enable() {
		this.enabled = true;

		this.prestigeConfig = new PrestigeConfig(this);
		this.prestigeConfig.load();

		this.prestigeManager = new PrestigeManager(this);
		this.prestigeManager.enable();

		this.api = new UltraPrisonPrestigesAPIImpl(this);

		this.savePlayerDataTask = new SavePlayerDataTask(this);
		this.savePlayerDataTask.start();

		this.registerCommands();
		this.registerListeners();
	}


	@Override
	public void disable() {
		this.savePlayerDataTask.stop();
		this.prestigeManager.disable();
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
						"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_prestige bigint, primary key (UUID))"
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
		new PrestigeCommand(this).register();
		new MaxPrestigeCommand(this).register();
		new PrestigeTopCommand(this).register();
		new PrestigeAdminCommand(this).register();
	}

	private void registerListeners() {
		new PrestigeListener(this).register();
	}
}
