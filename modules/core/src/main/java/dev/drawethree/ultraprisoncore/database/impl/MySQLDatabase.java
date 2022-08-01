package dev.drawethree.ultraprisoncore.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.DatabaseCredentials;
import dev.drawethree.ultraprisoncore.database.DatabaseType;
import dev.drawethree.ultraprisoncore.database.SQLDatabase;


public final class MySQLDatabase extends SQLDatabase {

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
        execute("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");
        for (UltraPrisonModule module : this.plugin.getModules()) {
            for (String sql : module.getCreateTablesSQL(this.getDatabaseType())) {
                execute(sql);
            }
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
}