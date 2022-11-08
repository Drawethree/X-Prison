package dev.drawethree.ultraprisoncore.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.PooledSQLDatabase;
import dev.drawethree.ultraprisoncore.database.model.ConnectionProperties;
import dev.drawethree.ultraprisoncore.database.model.SQLDatabaseType;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class SQLiteDatabase extends PooledSQLDatabase {

    private static final String FILE_NAME = "playerdata.db";

    private final String filePath;
    private final ConnectionProperties connectionProperties;

    public SQLiteDatabase(UltraPrisonCore plugin, ConnectionProperties connectionProperties) {
        super(plugin);
        this.connectionProperties = connectionProperties;
        this.filePath = this.plugin.getDataFolder().getPath() + File.separator + FILE_NAME;
    }

    @Override
    public SQLDatabaseType getDatabaseType() {
        return SQLDatabaseType.SQLITE;
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
        this.executeSqlAsync("INSERT OR IGNORE INTO " + UltraPrisonGangs.TABLE_NAME + "(UUID,name,owner,members) VALUES(?,?,?,?)", g.getUuid().toString(), g.getName(), g.getGangOwner().toString(), "");
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
    public boolean resetData(UltraPrisonModule module) {
            for (String table : module.getTables()) {
                executeSqlAsync("DELETE FROM " + table);
            }
        return true;
    }

}
