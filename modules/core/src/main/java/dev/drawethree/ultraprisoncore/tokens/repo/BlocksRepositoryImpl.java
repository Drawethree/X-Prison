package dev.drawethree.ultraprisoncore.tokens.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BlocksRepositoryImpl implements BlocksRepository {


	private static final String BLOCKS_UUID_COLNAME = "UUID";
	private static final String BLOCKS_BLOCKS_COLNAME = "Blocks";

	private final Database database;

	public BlocksRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public void resetBlocksWeekly(CommandSender sender) {
		this.executeSqlAsync("DELETE FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY);
	}

	@Override
	public long getPlayerBrokenBlocks(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public long getPlayerBrokenBlocksWeekly(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateBlocks(OfflinePlayer player, long newAmount) {
		this.execute("UPDATE " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " SET " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + "=? WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", newAmount, player.getUniqueId().toString());
	}

	@Override
	public void updateBlocksWeekly(OfflinePlayer player, long newAmount) {
		this.execute("UPDATE " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " SET " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + "=? WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", newAmount, player.getUniqueId().toString());
	}

	@Override
	public void addIntoBlocks(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoBlocksWeekly(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public Map<UUID, Long> getTopBlocksWeekly(int amountOfRecords) {
		Map<UUID, Long> topBlocksWeekly = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.BLOCKS_UUID_COLNAME + "," + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " ORDER BY " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				topBlocksWeekly.put(UUID.fromString(set.getString(MySQLDatabase.BLOCKS_UUID_COLNAME)), set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topBlocksWeekly;
	}

	@Override
	public Map<UUID, Long> getTopBlocks(int amountOfRecords) {
		Map<UUID, Long> topBlocks = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.BLOCKS_UUID_COLNAME + "," + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " ORDER BY " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				topBlocks.put(UUID.fromString(set.getString(MySQLDatabase.BLOCKS_UUID_COLNAME)), set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topBlocks;
	}
}
