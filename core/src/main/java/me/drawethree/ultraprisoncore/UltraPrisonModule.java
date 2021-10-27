package me.drawethree.ultraprisoncore;

import me.drawethree.ultraprisoncore.database.DatabaseType;

public interface UltraPrisonModule {

	void enable();

	void disable();

	void reload();

	boolean isEnabled();

	String getName();

	String[] getTables();

	String[] getCreateTablesSQL(DatabaseType type);

}
