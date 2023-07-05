package dev.drawethree.xprison.tokens.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import dev.drawethree.xprison.tokens.repo.BlocksRepository;
import org.bukkit.OfflinePlayer;

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
    private static final String TABLE_NAME_BLOCKS = "UltraPrison_BlocksBroken";
    private static final String TABLE_NAME_BLOCKS_WEEKLY = "UltraPrison_BlocksBrokenWeekly";

    private final SQLDatabase database;

    public BlocksRepositoryImpl(SQLDatabase database) {
        this.database = database;
    }

    @Override
    public void resetBlocksWeekly() {
        this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME_BLOCKS_WEEKLY);
    }

    @Override
    public long getPlayerBrokenBlocks(OfflinePlayer player) {
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT * FROM " + TABLE_NAME_BLOCKS + " WHERE " + BLOCKS_UUID_COLNAME + "=?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getLong(BLOCKS_BLOCKS_COLNAME);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getPlayerBrokenBlocksWeekly(OfflinePlayer player) {
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT * FROM " + TABLE_NAME_BLOCKS_WEEKLY + " WHERE " + BLOCKS_UUID_COLNAME + "=?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getLong(BLOCKS_BLOCKS_COLNAME);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void updateBlocks(OfflinePlayer player, long newAmount) {
        this.database.executeSql("UPDATE " + TABLE_NAME_BLOCKS + " SET " + BLOCKS_BLOCKS_COLNAME + "=? WHERE " + BLOCKS_UUID_COLNAME + "=?", newAmount, player.getUniqueId().toString());
    }

    @Override
    public void updateBlocksWeekly(OfflinePlayer player, long newAmount) {
        this.database.executeSql("UPDATE " + TABLE_NAME_BLOCKS_WEEKLY + " SET " + BLOCKS_BLOCKS_COLNAME + "=? WHERE " + BLOCKS_UUID_COLNAME + "=?", newAmount, player.getUniqueId().toString());
    }

    @Override
    public void addIntoBlocks(OfflinePlayer player) {
        String sql = database.getDatabaseType() == SQLDatabaseType.SQLITE ? "INSERT OR IGNORE INTO " + TABLE_NAME_BLOCKS + " VALUES(?,?)" : "INSERT IGNORE INTO " + TABLE_NAME_BLOCKS + " VALUES(?,?)";
        this.database.executeSql(sql, player.getUniqueId().toString(), 0);
    }

    @Override
    public void addIntoBlocksWeekly(OfflinePlayer player) {
        String sql = database.getDatabaseType() == SQLDatabaseType.SQLITE ? "INSERT OR IGNORE INTO " + TABLE_NAME_BLOCKS_WEEKLY + " VALUES(?,?)" : "INSERT IGNORE INTO " + TABLE_NAME_BLOCKS_WEEKLY + " VALUES(?,?)";
        this.database.executeSql(sql, player.getUniqueId().toString(), 0);
    }

    @Override
    public Map<UUID, Long> getTopBlocksWeekly(int amountOfRecords) {
        Map<UUID, Long> topBlocksWeekly = new LinkedHashMap<>();
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT " + BLOCKS_UUID_COLNAME + "," + BLOCKS_BLOCKS_COLNAME + " FROM " + TABLE_NAME_BLOCKS_WEEKLY + " ORDER BY " + BLOCKS_BLOCKS_COLNAME + " DESC LIMIT " + amountOfRecords); ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                topBlocksWeekly.put(UUID.fromString(set.getString(BLOCKS_UUID_COLNAME)), set.getLong(BLOCKS_BLOCKS_COLNAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topBlocksWeekly;
    }

    @Override
    public Map<UUID, Long> getTopBlocks(int amountOfRecords) {
        Map<UUID, Long> topBlocks = new LinkedHashMap<>();
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT " + BLOCKS_UUID_COLNAME + "," + BLOCKS_BLOCKS_COLNAME + " FROM " + TABLE_NAME_BLOCKS + " ORDER BY " + BLOCKS_BLOCKS_COLNAME + " DESC LIMIT " + amountOfRecords); ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                topBlocks.put(UUID.fromString(set.getString(BLOCKS_UUID_COLNAME)), set.getLong(BLOCKS_BLOCKS_COLNAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topBlocks;
    }

    @Override
    public void createTables() {
        this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BLOCKS + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))");
        this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_BLOCKS_WEEKLY + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))");
    }

    @Override
    public void clearTableData() {
        this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME_BLOCKS);
    }
}
