package dev.drawethree.xprison.prestiges.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import dev.drawethree.xprison.prestiges.repo.PrestigeRepository;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PrestigeRepositoryImpl implements PrestigeRepository {

    private static final String TABLE_NAME = "UltraPrison_Prestiges";
    private static final String PRESTIGES_UUID_COLNAME = "UUID";
    private static final String PRESTIGES_PRESTIGE_COLNAME = "id_prestige";

    private final SQLDatabase database;

    public PrestigeRepositoryImpl(SQLDatabase database) {
        this.database = database;
    }

    @Override
    public void updatePrestige(OfflinePlayer player, long newPrestige) {
        this.database.executeSql("UPDATE " + TABLE_NAME + " SET " + PRESTIGES_PRESTIGE_COLNAME + "=? WHERE " + PRESTIGES_UUID_COLNAME + "=?", newPrestige, player.getUniqueId().toString());
    }

    @Override
    public void addIntoPrestiges(OfflinePlayer player) {
        String sql = this.database.getDatabaseType() == SQLDatabaseType.SQLITE ? "INSERT OR IGNORE INTO " + TABLE_NAME + "(UUID,id_prestige)  VALUES(?,?)" : "INSERT IGNORE INTO " + TABLE_NAME + "(UUID,id_prestige) VALUES(?,?)";
        this.database.executeSql(sql, player.getUniqueId().toString(), 0);
    }

    @Override
    public long getPlayerPrestige(OfflinePlayer player) {
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT * FROM " + TABLE_NAME + " WHERE " + PRESTIGES_UUID_COLNAME + "=?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getLong(PRESTIGES_PRESTIGE_COLNAME);
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
        try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con, "SELECT " + PRESTIGES_UUID_COLNAME + "," + PRESTIGES_PRESTIGE_COLNAME + " FROM " + TABLE_NAME + " ORDER BY " + PRESTIGES_PRESTIGE_COLNAME + " DESC LIMIT " + amountOfRecords); ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                top10Prestige.put(UUID.fromString(set.getString(PRESTIGES_UUID_COLNAME)), set.getLong(PRESTIGES_PRESTIGE_COLNAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top10Prestige;
    }

    @Override
    public void createTables() {
        this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_prestige bigint, primary key (UUID))");
    }

    @Override
    public void clearTableData() {
        this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME);
    }
}
