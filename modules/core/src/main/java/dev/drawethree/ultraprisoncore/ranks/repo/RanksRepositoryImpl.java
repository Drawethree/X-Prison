package dev.drawethree.ultraprisoncore.ranks.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.SQLDatabase;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RanksRepositoryImpl implements RanksRepository {


	private static final String RANKS_UUID_COLNAME = "UUID";
	private static final String RANKS_RANK_COLNAME = "id_rank";

	private final Database database;

	public RanksRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public int getPlayerRank(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonRanks.TABLE_NAME + " WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getInt(SQLDatabase.RANKS_RANK_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateRank(OfflinePlayer player, int newRank) {
		this.execute("UPDATE " + UltraPrisonRanks.TABLE_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", newRank, player.getUniqueId().toString());
	}

	@Override
	public void addIntoRanks(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonRanks.TABLE_NAME + "(UUID,id_rank) VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

}
