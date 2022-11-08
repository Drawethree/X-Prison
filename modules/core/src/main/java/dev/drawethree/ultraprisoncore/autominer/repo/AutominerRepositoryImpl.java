package dev.drawethree.ultraprisoncore.autominer.repo;

import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutominerRepositoryImpl implements AutominerRepository {

	private static final String AUTOMINER_UUID_COLNAME = "UUID";
	private static final String AUTOMINER_TIME_COLNAME = "time";
	private final Database database;


	public AutominerRepositoryImpl(Database database) {

		this.database = database;
	}

	@Override
	public int getPlayerAutoMinerTime(OfflinePlayer p) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonAutoMiner.TABLE_NAME + " WHERE " + MySQLDatabase.AUTOMINER_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getInt(MySQLDatabase.AUTOMINER_TIME_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void removeExpiredAutoMiners() {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM " + UltraPrisonAutoMiner.TABLE_NAME + " WHERE " + MySQLDatabase.AUTOMINER_TIME_COLNAME + " <= 0")) {
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveAutoMiner(Player p, int timeLeft) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + UltraPrisonAutoMiner.TABLE_NAME + " VALUES (?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.AUTOMINER_TIME_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			statement.setInt(2, timeLeft);
			statement.setInt(3, timeLeft);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
