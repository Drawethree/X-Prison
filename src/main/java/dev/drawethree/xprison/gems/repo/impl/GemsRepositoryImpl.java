package dev.drawethree.xprison.gems.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import dev.drawethree.xprison.gems.repo.GemsRepository;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class GemsRepositoryImpl implements GemsRepository {

	private static final String TABLE_NAME = "UltraPrison_Gems";
	private static final String GEMS_UUID_COLNAME = "UUID";
	private static final String GEMS_GEMS_COLNAME = "Gems";

	private final SQLDatabase database;

	public GemsRepositoryImpl(SQLDatabase database) {
		this.database = database;
	}

	@Override
	public long getPlayerGems(OfflinePlayer p) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"SELECT * FROM " + TABLE_NAME + " WHERE " + GEMS_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(GEMS_GEMS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateGems(OfflinePlayer p, long newAmount) {
		this.database.executeSql("UPDATE " + TABLE_NAME + " SET " + GEMS_GEMS_COLNAME + "=? WHERE " + GEMS_UUID_COLNAME + "=?", newAmount, p.getUniqueId().toString());
	}

	@Override
	public Map<UUID, Long> getTopGems(int amountOfRecords) {
		Map<UUID, Long> topGems = new LinkedHashMap<>();
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"SELECT " + GEMS_UUID_COLNAME + "," + GEMS_GEMS_COLNAME + " FROM " + TABLE_NAME + " ORDER BY " + GEMS_GEMS_COLNAME + " DESC LIMIT " + amountOfRecords); ResultSet set = statement.executeQuery()) {
			while (set.next()) {
				topGems.put(UUID.fromString(set.getString(GEMS_UUID_COLNAME)), set.getLong(GEMS_GEMS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topGems;
	}

	@Override
	public void addIntoGems(OfflinePlayer player, long startingGems) {
		String sql = this.database.getDatabaseType() == SQLDatabaseType.SQLITE ? "INSERT OR IGNORE INTO " + TABLE_NAME + " VALUES(?,?)" : "INSERT IGNORE INTO " + TABLE_NAME + " VALUES(?,?)";
		this.database.executeSql(sql, player.getUniqueId().toString(), startingGems);
	}

	@Override
	public void createTables() {
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, Gems bigint, primary key (UUID))");
	}

	@Override
	public void clearTableData() {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME);
	}
}
