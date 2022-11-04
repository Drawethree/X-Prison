package dev.drawethree.ultraprisoncore.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.database.SQLDatabase;
import dev.drawethree.ultraprisoncore.database.model.ConnectionProperties;
import dev.drawethree.ultraprisoncore.database.model.DatabaseType;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.model.GangInvitation;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.history.UltraPrisonHistory;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.lucko.helper.utils.Log;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SQLiteDatabase extends SQLDatabase {

    private static final String FILE_NAME = "playerdata.db";

    private final String filePath;
    private final ConnectionProperties connectionProperties;

    public SQLiteDatabase(UltraPrisonCore plugin, ConnectionProperties connectionProperties) {
        super(plugin);
        this.connectionProperties = connectionProperties;
        this.filePath = this.plugin.getDataFolder().getPath() + File.separator + FILE_NAME;
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLITE;
    }

    @Override
    public void connect() {

        this.createDBFile();

        final HikariConfig hikari = new HikariConfig();

        hikari.setPoolName("ultraprison-" + POOL_COUNTER.getAndIncrement());

        hikari.setDriverClassName("org.sqlite.JDBC");
        hikari.setJdbcUrl("jdbc:sqlite:" + this.filePath);

        hikari.setConnectionTimeout(connectionProperties.getConnectionTimeout());
        hikari.setIdleTimeout(connectionProperties.getIdleTimeout());
        hikari.setKeepaliveTime(connectionProperties.getKeepAliveTime());
        hikari.setMaxLifetime(connectionProperties.getMaxLifetime());
        hikari.setMinimumIdle(connectionProperties.getMinimumIdle());
        hikari.setMaximumPoolSize(connectionProperties.getMaximumPoolSize());
        hikari.setLeakDetectionThreshold(connectionProperties.getLeakDetectionThreshold());
        hikari.setConnectionTestQuery(connectionProperties.getTestQuery());

        this.hikari = new HikariDataSource(hikari);
    }

    private void createDBFile() {
        File dbFile = new File(this.filePath);
        try {
            dbFile.createNewFile();
        } catch (IOException e) {
            this.plugin.getLogger().warning(String.format("Unable to create %s", FILE_NAME));
            e.printStackTrace();
        }
    }

    @Override
    public void createTables() {
        execute("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");
        for (UltraPrisonModule module : this.plugin.getModules()) {
            for (String sql : module.getCreateTablesSQL(this.getDatabaseType())) {
                execute(sql);
            }
        }
    }

    @Override
    public void createIndexes() {
        this.executeAsync(String.format("CREATE INDEX IF NOT EXISTS %s ON %s (%s)", INDEX_HISTORY_PLAYER, UltraPrisonHistory.TABLE_NAME, HISTORY_PLAYER_UUID_COLNAME));
    }

    @Override
    public void addIntoTokens(OfflinePlayer player, long startingTokens) {
        this.execute("INSERT OR IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_TOKENS + " VALUES(?,?)", player.getUniqueId().toString(), startingTokens);
    }

    @Override
    public void addIntoRanks(OfflinePlayer player) {
        this.execute("INSERT OR IGNORE INTO " + UltraPrisonRanks.TABLE_NAME + "(UUID,id_rank) VALUES(?,?)", player.getUniqueId().toString(), 0);
    }

    @Override
    public void addIntoPrestiges(OfflinePlayer player) {
        this.execute("INSERT OR IGNORE INTO " + UltraPrisonPrestiges.TABLE_NAME + "(UUID,id_prestige)  VALUES(?,?)", player.getUniqueId().toString(), 0);
    }

    @Override
    public void addIntoBlocks(OfflinePlayer player) {
        this.execute("INSERT OR IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " VALUES(?,?)", player.getUniqueId().toString(), 0);
    }

    @Override
    public void addIntoBlocksWeekly(OfflinePlayer player) {
        this.execute("INSERT OR IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " VALUES(?,?)", player.getUniqueId().toString(), 0);
    }

    @Override
    public void addIntoGems(OfflinePlayer player, long startingGems) {
        this.execute("INSERT OR IGNORE INTO " + UltraPrisonGems.TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), startingGems);
    }

    @Override
    public void createGang(Gang g) {
        this.executeAsync("INSERT OR IGNORE INTO " + UltraPrisonGangs.TABLE_NAME + "(UUID,name,owner,members) VALUES(?,?,?,?)", g.getUuid().toString(), g.getName(), g.getGangOwner().toString(), "");
    }

    @Override
    public void saveAutoMiner(Player p, int timeLeft) {
        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + UltraPrisonAutoMiner.TABLE_NAME + " VALUES (?,?) ")) {
            statement.setString(1, p.getUniqueId().toString());
            statement.setInt(2, timeLeft);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSellMultiplier(Player player, PlayerMultiplier multiplier) {

        if (multiplier == null || !multiplier.isValid()) {
            this.deleteSellMultiplier(player);
            return;
        }

        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + UltraPrisonMultipliers.TABLE_NAME + " VALUES(?,?,?)")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setDouble(2, multiplier.getMultiplier());
            statement.setLong(3, multiplier.getEndTime());
            statement.execute();
        } catch (SQLException e) {
            this.plugin.getLogger().warning("Could not save sell multiplier for player " + player.getName() + "!");
            e.printStackTrace();
        }
    }

    @Override
    public void saveTokenMultiplier(Player player, PlayerMultiplier multiplier) {

        if (multiplier == null || !multiplier.isValid()) {
            this.deleteTokenMultiplier(player);
            return;
        }

        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + UltraPrisonMultipliers.TABLE_NAME_TOKEN + " VALUES(?,?,?)")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setDouble(2, multiplier.getMultiplier());
            statement.setLong(3, multiplier.getEndTime());
            statement.execute();
        } catch (SQLException e) {
            this.plugin.getLogger().warning("Could not save token multiplier for player " + player.getName() + "!");
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayerNickname(OfflinePlayer player) {
        this.executeAsync("INSERT OR REPLACE INTO " + MySQLDatabase.UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), player.getName());
    }

    @Override
    public List<Gang> getAllGangs() {
        List<Gang> returnList = new ArrayList<>();
        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonGangs.TABLE_NAME)) {
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    Gang gang = new Gang();

                    UUID gangUUID;
                    try {
                        gangUUID = UUID.fromString(set.getString(GANGS_UUID_COLNAME));
                    } catch (Exception e) {
                        gangUUID = UUID.randomUUID();
                        set.updateString(GANGS_UUID_COLNAME, gangUUID.toString());
                        set.updateRow();
                    }

                    gang.setUuid(gangUUID);

                    String gangName = set.getString(GANGS_NAME_COLNAME);
                    gang.setName(gangName);
                    UUID owner = UUID.fromString(set.getString(GANGS_OWNER_COLNAME));
                    gang.setGangOwner(owner);
                    List<UUID> members = new ArrayList<>();

                    for (String s : set.getString(GANGS_MEMBERS_COLNAME).split(",")) {
                        if (s.isEmpty()) {
                            continue;
                        }
                        try {
                            UUID uuid = UUID.fromString(s);
                            members.add(uuid);
                        } catch (Exception e) {
                            Log.warn("Unable to fetch UUID: " + s);
                            e.printStackTrace();
                        }
                    }
                    gang.setGangMembers(members);

                    int value = set.getInt(GANGS_VALUE_COLNAME);
                    gang.setValue(value);
                    List<GangInvitation> gangInvitations = getGangInvitations(gang);
                    gang.setPendingInvites(gangInvitations);

                    returnList.add(gang);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnList;
    }

    @Override
    public boolean resetData(UltraPrisonModule module) {
            for (String table : module.getTables()) {
                executeAsync("DELETE FROM " + table);
            }
        return true;
    }

}