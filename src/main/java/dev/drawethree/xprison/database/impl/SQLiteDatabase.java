package dev.drawethree.xprison.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.database.PooledSQLDatabase;
import dev.drawethree.xprison.database.model.ConnectionProperties;
import dev.drawethree.xprison.database.model.SQLDatabaseType;

import java.io.File;
import java.io.IOException;

public final class SQLiteDatabase extends PooledSQLDatabase {

    private static final String FILE_NAME = "playerdata.db";

    private final String filePath;
    private final ConnectionProperties connectionProperties;

    public SQLiteDatabase(XPrison plugin, ConnectionProperties connectionProperties) {
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

        hikari.setPoolName("xprison-" + POOL_COUNTER.getAndIncrement());

        hikari.setDriverClassName("org.sqlite.JDBC");
        hikari.setJdbcUrl("jdbc:sqlite:" + this.filePath);

        hikari.setConnectionTimeout(connectionProperties.getConnectionTimeout());
        hikari.setIdleTimeout(connectionProperties.getIdleTimeout());
        hikari.setKeepaliveTime(connectionProperties.getKeepAliveTime());
        hikari.setMaxLifetime(connectionProperties.getMaxLifetime());
        hikari.setMinimumIdle(connectionProperties.getMinimumIdle());
        hikari.setMaximumPoolSize(1);
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

}
