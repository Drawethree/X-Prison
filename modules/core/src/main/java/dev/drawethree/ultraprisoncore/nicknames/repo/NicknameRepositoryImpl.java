package dev.drawethree.ultraprisoncore.nicknames.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import org.bukkit.OfflinePlayer;

public class NicknameRepositoryImpl implements NicknameRepository {


	private static final String UUID_PLAYERNAME_TABLE_NAME = "UltraPrison_Nicknames";
	private static final String UUID_PLAYERNAME_UUID_COLNAME = "UUID";
	private static final String UUID_PLAYERNAME_NICK_COLNAME = "nickname";

	private final Database database;

	public NicknameRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public void updatePlayerNickname(OfflinePlayer player) {
		this.executeSqlAsync("INSERT INTO " + UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + UUID_PLAYERNAME_NICK_COLNAME + "=?", player.getUniqueId().toString(), player.getName(), player.getName());
		this.executeSqlAsync("INSERT OR REPLACE INTO " + MySQLDatabase.UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), player.getName());
	}

	@Override
	public void createTables() {
		execute("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");
	}

	@Override
	public void resetData() {
		// Nothing.
	}
}
