package me.drawethree.ultraprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.database.DatabaseCredentials;
import me.drawethree.ultraprisoncore.database.DatabaseType;
import me.drawethree.ultraprisoncore.database.SQLDatabase;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MySQLDatabase extends SQLDatabase {

    private final DatabaseCredentials credentials;

    public MySQLDatabase(UltraPrisonCore parent, DatabaseCredentials credentials) {
        super(parent);

        this.plugin.getLogger().info("Using MySQL (remote) database.");

        this.credentials = credentials;
        this.connect();
    }


    @Override
    public void connect() {
        final HikariConfig hikari = new HikariConfig();

        hikari.setPoolName("ultraprison-" + POOL_COUNTER.getAndIncrement());
        hikari.setJdbcUrl("jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabaseName());
        hikari.setConnectionTestQuery("SELECT 1");

        hikari.setUsername(credentials.getUserName());
        hikari.setPassword(credentials.getPassword());

        hikari.setMinimumIdle(MINIMUM_IDLE);
        hikari.setMaxLifetime(MAX_LIFETIME);
        hikari.setConnectionTimeout(CONNECTION_TIMEOUT);
        hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        hikari.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

        this.hikari = new HikariDataSource(hikari);
    }


    @Override
    public void runSQLUpdates() {
    }

    @Override
    public void createTables() {

        for (UltraPrisonModule module : this.plugin.getModules()) {
            for (String sql : module.getCreateTablesSQL(this.getDatabaseType())) {
                execute(sql);
            }
        }

        //TODO: Separate module for handling this
        execute("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");

        // v1.4.7-BETA - Added UUID column to UltraPrison_Gangs table as primary key
        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='" + UltraPrisonGangs.TABLE_NAME + "' AND column_name ='uuid'")) {
            try (ResultSet set = statement.executeQuery()) {
                if (!set.next()) {
                    execute("alter table " + UltraPrisonGangs.TABLE_NAME + " drop primary key", null);
                    execute("alter table " + UltraPrisonGangs.TABLE_NAME + " add column uuid varchar(36) not null first", null);
                    execute("alter table " + UltraPrisonGangs.TABLE_NAME + " add primary key(uuid,name)", null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // v1.5.0 - Renamed multipliers table columns

        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='" + UltraPrisonMultipliers.TABLE_NAME + "' AND column_name ='vote_multiplier'")) {
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    execute("alter table " + UltraPrisonMultipliers.TABLE_NAME + " rename column vote_multiplier to sell_multiplier", null);
                    execute("alter table " + UltraPrisonMultipliers.TABLE_NAME + " rename column vote_multiplier_timeleft to sell_multiplier_timeleft", null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
}