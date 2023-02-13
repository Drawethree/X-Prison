package dev.drawethree.xprison.nicknames.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import dev.drawethree.xprison.nicknames.repo.NicknameRepository;
import org.bukkit.OfflinePlayer;

public class NicknameRepositoryImpl implements NicknameRepository {

	private static final String UUID_PLAYERNAME_TABLE_NAME = "UltraPrison_Nicknames";
	private static final String UUID_PLAYERNAME_UUID_COLNAME = "UUID";
	private static final String UUID_PLAYERNAME_NICK_COLNAME = "nickname";

	private final SQLDatabase database;

	public NicknameRepositoryImpl(SQLDatabase database) {
		this.database = database;
	}

	@Override
	public void updatePlayerNickname(OfflinePlayer player) {
		if (database.getDatabaseType() == SQLDatabaseType.MYSQL) {
			this.database.executeSqlAsync("INSERT INTO " + UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + UUID_PLAYERNAME_NICK_COLNAME + "=?", player.getUniqueId().toString(), player.getName(), player.getName());
		} else {
			this.database.executeSqlAsync("INSERT OR REPLACE INTO " + UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), player.getName());
		}
	}


	@Override
	public void createTables() {
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");
	}

	@Override
	public void resetData() {
		// Nothing.
	}
}
