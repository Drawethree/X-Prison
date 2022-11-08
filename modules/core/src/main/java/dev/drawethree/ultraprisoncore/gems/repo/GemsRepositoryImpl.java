package dev.drawethree.ultraprisoncore.gems.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class GemsRepositoryImpl implements GemsRepository {

	private static final String GEMS_UUID_COLNAME = "UUID";
	private static final String GEMS_GEMS_COLNAME = "Gems";

	private final Database database;

	public GemsRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public long getPlayerGems(OfflinePlayer p) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonGems.TABLE_NAME + " WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.GEMS_GEMS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateGems(OfflinePlayer p, long newAmount) {
		this.execute("UPDATE " + UltraPrisonGems.TABLE_NAME + " SET " + MySQLDatabase.GEMS_GEMS_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", newAmount, p.getUniqueId().toString());
	}

	@Override
	public Map<UUID, Long> getTopGems(int amountOfRecords) {
		Map<UUID, Long> topGems = new LinkedHashMap<>();
		try (Connection con = this.database.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.GEMS_UUID_COLNAME + "," + MySQLDatabase.GEMS_GEMS_COLNAME + " FROM " + UltraPrisonGems.TABLE_NAME + " ORDER BY " + MySQLDatabase.GEMS_GEMS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				topGems.put(UUID.fromString(set.getString(MySQLDatabase.GEMS_UUID_COLNAME)), set.getLong(MySQLDatabase.GEMS_GEMS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topGems;
	}

	@Override
	public void addIntoGems(OfflinePlayer player, long startingGems) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonGems.TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), startingGems);
	}
}
