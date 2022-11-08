package dev.drawethree.ultraprisoncore.prestiges.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.SQLDatabase;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PrestigesRepositoryImpl implements PrestigesRepository {

	private static final String PRESTIGES_UUID_COLNAME = "UUID";
	private static final String PRESTIGES_PRESTIGE_COLNAME = "id_prestige";

	private final Database database;

	public PrestigesRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public void updatePrestige(OfflinePlayer player, long newPrestige) {
		this.execute("UPDATE " + UltraPrisonPrestiges.TABLE_NAME + " SET " + MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.PRESTIGES_UUID_COLNAME + "=?", newPrestige, player.getUniqueId().toString());
	}

	@Override
	public void addIntoPrestiges(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonPrestiges.TABLE_NAME + "(UUID,id_prestige) VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public long getPlayerPrestige(OfflinePlayer player) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonPrestiges.TABLE_NAME + " WHERE " + MySQLDatabase.PRESTIGES_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(SQLDatabase.PRESTIGES_PRESTIGE_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public Map<UUID, Long> getTopPrestiges(int amountOfRecords) {
		Map<UUID, Long> top10Prestige = new LinkedHashMap<>();
		try (Connection con = this.database.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.PRESTIGES_UUID_COLNAME + "," + MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME + " FROM " + UltraPrisonPrestiges.TABLE_NAME + " ORDER BY " + MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				top10Prestige.put(UUID.fromString(set.getString(MySQLDatabase.PRESTIGES_UUID_COLNAME)), set.getLong(MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return top10Prestige;
	}
}
