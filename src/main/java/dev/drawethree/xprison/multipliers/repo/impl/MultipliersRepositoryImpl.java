package dev.drawethree.xprison.multipliers.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import dev.drawethree.xprison.multipliers.enums.MultiplierType;
import dev.drawethree.xprison.multipliers.multiplier.PlayerMultiplier;
import dev.drawethree.xprison.multipliers.repo.MultipliersRepository;
import me.lucko.helper.time.Time;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MultipliersRepositoryImpl implements MultipliersRepository {

	private static final String TABLE_NAME = "UltraPrison_Multipliers";
	private static final String TABLE_NAME_TOKEN = "UltraPrison_Token_Multipliers";

	private static final String MULTIPLIERS_UUID_COLNAME = "UUID";
	private static final String MULTIPLIERS_MULTIPLIER_COLNAME = "sell_multiplier";
	private static final String MULTIPLIERS_TIMELEFT_COLNAME = "sell_multiplier_timeleft";

	private static final String MULTIPLIERS_TOKEN_UUID_COLNAME = "UUID";
	private static final String MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME = "token_multiplier";
	private static final String MULTIPLIERS_TOKEN_TIMELEFT_COLNAME = "token_multiplier_timeleft";

	private final SQLDatabase database;

	public MultipliersRepositoryImpl(SQLDatabase database) {
		this.database = database;
	}


	@Override
	public PlayerMultiplier getSellMultiplier(Player player) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"SELECT * FROM " + TABLE_NAME + " WHERE " + MULTIPLIERS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					double multiplier = set.getDouble(MULTIPLIERS_MULTIPLIER_COLNAME);
					long endTime = set.getLong(MULTIPLIERS_TIMELEFT_COLNAME);
					if (endTime > Time.nowMillis()) {
						return new PlayerMultiplier(player.getUniqueId(), multiplier, endTime, MultiplierType.SELL);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PlayerMultiplier getTokenMultiplier(Player player) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"SELECT * FROM " + TABLE_NAME_TOKEN + " WHERE " + MULTIPLIERS_TOKEN_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					double multiplier = set.getDouble(MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME);
					long endTime = set.getLong(MULTIPLIERS_TOKEN_TIMELEFT_COLNAME);
					if (endTime > Time.nowMillis()) {
						return new PlayerMultiplier(player.getUniqueId(), multiplier, endTime, MultiplierType.TOKENS);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void removeExpiredMultipliers() {
		long time = Time.nowMillis();
		try (Connection con = this.database.getConnection();
			 PreparedStatement statement = database.prepareStatement(con,"DELETE FROM " + TABLE_NAME + " WHERE " + MULTIPLIERS_TIMELEFT_COLNAME + " < " + time);
			 PreparedStatement statement2 = database.prepareStatement(con,"DELETE FROM " + TABLE_NAME_TOKEN + " WHERE " + MULTIPLIERS_TOKEN_TIMELEFT_COLNAME + " < " + time)) {
			statement.execute();
			statement2.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createTables() {
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, sell_multiplier double, sell_multiplier_timeleft long, primary key (UUID))");
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TOKEN + "(UUID varchar(36) NOT NULL UNIQUE, token_multiplier double, token_multiplier_timeleft long, primary key (UUID))");
	}

	@Override
	public void clearTableData() {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME);
	}

	@Override
	public void saveSellMultiplier(Player player, PlayerMultiplier multiplier) {

		if (multiplier == null || !multiplier.isValid()) {
			this.deleteSellMultiplier(player);
			return;
		}

		if (database.getDatabaseType() == SQLDatabaseType.MYSQL) {
			try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"INSERT INTO " + TABLE_NAME + " VALUES(?,?,?) ON DUPLICATE KEY UPDATE " + MULTIPLIERS_MULTIPLIER_COLNAME + "=?, " + MULTIPLIERS_TIMELEFT_COLNAME + "=?")) {
				statement.setString(1, player.getUniqueId().toString());
				statement.setDouble(2, multiplier.getMultiplier());
				statement.setLong(3, multiplier.getEndTime());
				statement.setDouble(4, multiplier.getMultiplier());
				statement.setLong(5, multiplier.getEndTime());
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"INSERT OR REPLACE INTO " + TABLE_NAME + " VALUES(?,?,?)")) {
				statement.setString(1, player.getUniqueId().toString());
				statement.setDouble(2, multiplier.getMultiplier());
				statement.setLong(3, multiplier.getEndTime());
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void deleteSellMultiplier(Player player) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"DELETE FROM " + TABLE_NAME + " WHERE " + MULTIPLIERS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteTokenMultiplier(Player player) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"DELETE FROM " + TABLE_NAME_TOKEN + " WHERE " + MULTIPLIERS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveTokenMultiplier(Player player, PlayerMultiplier multiplier) {

		if (multiplier == null || !multiplier.isValid()) {
			this.deleteTokenMultiplier(player);
			return;
		}

		if (database.getDatabaseType() == SQLDatabaseType.MYSQL) {
			try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"INSERT INTO " + TABLE_NAME_TOKEN + " VALUES(?,?,?) ON DUPLICATE KEY UPDATE " + MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME + "=?, " + MULTIPLIERS_TOKEN_TIMELEFT_COLNAME + "=?")) {
				statement.setString(1, player.getUniqueId().toString());
				statement.setDouble(2, multiplier.getMultiplier());
				statement.setLong(3, multiplier.getEndTime());
				statement.setDouble(4, multiplier.getMultiplier());
				statement.setLong(5, multiplier.getEndTime());
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"INSERT OR REPLACE INTO " + TABLE_NAME_TOKEN + " VALUES(?,?,?)")) {
				statement.setString(1, player.getUniqueId().toString());
				statement.setDouble(2, multiplier.getMultiplier());
				statement.setLong(3, multiplier.getEndTime());
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
