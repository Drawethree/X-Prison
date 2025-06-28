package dev.drawethree.xprison.nicknames.repo;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import org.bukkit.OfflinePlayer;

public class NicknameRepository {

	private static final String UUID_PLAYERNAME_TABLE_NAME = "UltraPrison_Nicknames";
	private static final String UUID_PLAYERNAME_NICK_COLNAME = "nickname";

	private final SQLDatabase database;

	public NicknameRepository(SQLDatabase database) {
		this.database = database;
	}

	public void updatePlayerNickname(OfflinePlayer player) {
		if (database.getDatabaseType() == SQLDatabaseType.MYSQL) {
			this.database.executeSqlAsync("INSERT INTO " + UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + UUID_PLAYERNAME_NICK_COLNAME + "=?", player.getUniqueId().toString(), player.getName(), player.getName());
		} else {
			this.database.executeSqlAsync("INSERT OR REPLACE INTO " + UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), player.getName());
		}
	}

	public void createTables() {
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");
	}
}
