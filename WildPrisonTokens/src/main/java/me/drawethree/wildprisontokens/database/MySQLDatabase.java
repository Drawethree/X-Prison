package me.drawethree.wildprisontokens.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.Schedulers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class MySQLDatabase {

    private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);
    private static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
    private static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

    private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30); // 30 Minutes
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10); // 10 seconds
    private static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10); // 10 seconds

    private static final String DATA_SOURCE_CLASS = "org.mariadb.jdbc.MySQLDataSource";

    public static final String TOKENS_DB_NAME = "WildPrison_Tokens";
    public static final String BLOCKS_DB_NAME = "WildPrison_BlocksBroken";

    public static final String TOKENS_UUID_COLNAME = "UUID";
    public static final String TOKENS_TOKENS_COLNAME = "Tokens";

    public static final String BLOCKS_UUID_COLNAME = "UUID";
    public static final String BLOCKS_BLOCKS_COLNAME = "Blocks";

    @Getter
    private WildPrisonTokens parent;
    @Getter
    private HikariDataSource hikari;
    private DatabaseCredentials credentials;

    public MySQLDatabase(WildPrisonTokens parent) {
        this.parent = parent;
        this.credentials = DatabaseCredentials.fromConfig(parent.getConfig());

        final HikariConfig hikari = new HikariConfig();

        hikari.setPoolName("wildprison-" + POOL_COUNTER.getAndIncrement());

        hikari.setJdbcUrl("jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabaseName());

        hikari.setUsername(credentials.getUserName());
        hikari.setPassword(credentials.getPassword());

        hikari.setMinimumIdle(MINIMUM_IDLE);
        hikari.setMaxLifetime(MAX_LIFETIME);
        hikari.setConnectionTimeout(CONNECTION_TIMEOUT);
        hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        hikari.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

        this.hikari = new HikariDataSource(hikari);
        this.connect();
    }


    private synchronized void connect() {
        Schedulers.async().run(() -> {
            try {
                createTables();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Always call async and close after!
    /*public synchronized ResultSet query(String sql, Object... replacements) {

        try (Connection connection = this.hikari.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (replacements != null) {
                for (int i = 0; i < replacements.length; i++) {
                    statement.setObject(i + 1, replacements[i]);
                }
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }*/

    private synchronized void createTables() {
        Schedulers.async().run(() -> {
            execute("CREATE TABLE IF NOT EXISTS " + TOKENS_DB_NAME + "(UUID varchar(36) NOT NULL, Tokens bigint, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_DB_NAME + "(UUID varchar(36) NOT NULL, Blocks bigint, primary key (UUID))");
        });
    }


    //Always execute async!
    public synchronized void execute(String sql, Object... replacements) {

        try (Connection c = hikari.getConnection(); PreparedStatement statement = c.prepareStatement(sql)) {
            if (replacements != null) {
                for (int i = 0; i < replacements.length; i++) {
                    statement.setObject(i + 1, replacements[i]);
                }
            }
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (hikari != null) {
            hikari.close();
            this.parent.getLogger().info("Closing SQL Connection");
        }
    }

}