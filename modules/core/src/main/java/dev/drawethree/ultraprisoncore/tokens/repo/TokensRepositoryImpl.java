package dev.drawethree.ultraprisoncore.tokens.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TokensRepositoryImpl implements TokensRepository {

	private static final String TOKENS_UUID_COLNAME = "UUID";
	private static final String TOKENS_TOKENS_COLNAME = "Tokens";

	private final Database database;

	public TokensRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public long getPlayerTokens(OfflinePlayer p) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonTokens.TABLE_NAME_TOKENS + " WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.TOKENS_TOKENS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateTokens(OfflinePlayer p, long amount) {
		this.execute("UPDATE " + UltraPrisonTokens.TABLE_NAME_TOKENS + " SET " + MySQLDatabase.TOKENS_TOKENS_COLNAME + "=? WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", amount, p.getUniqueId().toString());
	}

	@Override
	public Map<UUID, Long> getTopTokens(int amountOfRecords) {
		Map<UUID, Long> top10Tokens = new LinkedHashMap<>();
		try (Connection con = this.database.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.TOKENS_UUID_COLNAME + "," + MySQLDatabase.TOKENS_TOKENS_COLNAME + " FROM " + UltraPrisonTokens.TABLE_NAME_TOKENS + " ORDER BY " + MySQLDatabase.TOKENS_TOKENS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				top10Tokens.put(UUID.fromString(set.getString(MySQLDatabase.TOKENS_UUID_COLNAME)), set.getLong(MySQLDatabase.TOKENS_TOKENS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return top10Tokens;
	}

	@Override
	public void addIntoTokens(OfflinePlayer player, long startingTokens) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_TOKENS + " VALUES(?,?)", player.getUniqueId().toString(), startingTokens);
	}
}
