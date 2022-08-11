package dev.drawethree.ultraprisoncore;

import dev.drawethree.ultraprisoncore.database.model.DatabaseType;

public interface UltraPrisonModule {

	void enable();

	void disable();

	void reload();

	boolean isEnabled();

	String getName();

	String[] getTables();

	String[] getCreateTablesSQL(DatabaseType type);

	boolean isHistoryEnabled();

}
