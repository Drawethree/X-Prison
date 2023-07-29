package dev.drawethree.xprison.autominer.repo.impl;

import dev.drawethree.xprison.autominer.repo.AutominerRepository;
import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutominerRepositoryImpl implements AutominerRepository {

	private static final String TABLE_NAME = "UltraPrison_AutoMiner";
	private static final String AUTOMINER_UUID_COLNAME = "UUID";
	private static final String AUTOMINER_TIME_COLNAME = "time";
	private final SQLDatabase database;

	public AutominerRepositoryImpl(SQLDatabase database) {

		this.database = database;
	}

	@Override
	public int getPlayerAutoMinerTime(OfflinePlayer p) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT * FROM " + TABLE_NAME + " WHERE " + AUTOMINER_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getInt(AUTOMINER_TIME_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void removeExpiredAutoMiners() {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "DELETE FROM " + TABLE_NAME + " WHERE " + AUTOMINER_TIME_COLNAME + " <= 0")) {
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveAutoMiner(Player p, int timeLeft) {
		if (database.getDatabaseType() == SQLDatabaseType.MYSQL) {
			try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "INSERT INTO " + TABLE_NAME + " VALUES (?,?) ON DUPLICATE KEY UPDATE " + AUTOMINER_TIME_COLNAME + "=?")) {
				statement.setString(1, p.getUniqueId().toString());
				statement.setInt(2, timeLeft);
				statement.setInt(3, timeLeft);
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES(?,?)")) {
				statement.setString(1, p.getUniqueId().toString());
				statement.setDouble(2, timeLeft);
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void createTables() {
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, time int, primary key (UUID))");
	}

	@Override
	public void clearTableData() {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME);
	}
}
