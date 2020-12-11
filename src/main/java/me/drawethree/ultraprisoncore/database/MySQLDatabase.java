package me.drawethree.ultraprisoncore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.lucko.helper.Schedulers;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
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


    public static final String RANKS_DB_NAME = "UltraPrison_Ranks";
    public static final String TOKENS_DB_NAME = "UltraPrison_Tokens";
    public static final String GEMS_DB_NAME = "UltraPrison_Gems";
    public static final String BLOCKS_DB_NAME = "UltraPrison_BlocksBroken";
    public static final String BLOCKS_WEEKLY_DB_NAME = "UltraPrison_BlocksBrokenWeekly";
    public static final String MULTIPLIERS_DB_NAME = "UltraPrison_Multipliers";
    public static final String AUTOMINER_DB_NAME = "UltraPrison_AutoMiner";

    private static final String[] ALL_TABLES = new String[]{
            RANKS_DB_NAME,
            TOKENS_DB_NAME,
            GEMS_DB_NAME,
            BLOCKS_DB_NAME,
            BLOCKS_WEEKLY_DB_NAME,
            MULTIPLIERS_DB_NAME,
            AUTOMINER_DB_NAME,
    };

    public static final String RANKS_UUID_COLNAME = "UUID";
    public static final String RANKS_RANK_COLNAME = "id_rank";
    public static final String RANKS_PRESTIGE_COLNAME = "id_prestige";

    public static final String TOKENS_UUID_COLNAME = "UUID";
    public static final String TOKENS_TOKENS_COLNAME = "Tokens";

    public static final String GEMS_UUID_COLNAME = "UUID";
    public static final String GEMS_GEMS_COLNAME = "Gems";

    public static final String BLOCKS_UUID_COLNAME = "UUID";
    public static final String BLOCKS_BLOCKS_COLNAME = "Blocks";

    public static final String MULTIPLIERS_UUID_COLNAME = "UUID";
    public static final String MULTIPLIERS_VOTE_COLNAME = "vote_multiplier";
    public static final String MULTIPLIERS_VOTE_TIMELEFT_COLNAME = "vote_multiplier_timeleft";

    public static final String AUTOMINER_UUID_COLNAME = "UUID";
    public static final String AUTOMINER_TIME_COLNAME = "time";

    @Getter
    private UltraPrisonCore parent;
    @Getter
    private HikariDataSource hikari;
    private DatabaseCredentials credentials;

    public MySQLDatabase(UltraPrisonCore parent) {
        this.parent = parent;
        this.credentials = DatabaseCredentials.fromConfig(parent.getConfig());

        final HikariConfig hikari = new HikariConfig();

        hikari.setPoolName("ultraprison-" + POOL_COUNTER.getAndIncrement());

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
            execute("CREATE TABLE IF NOT EXISTS " + RANKS_DB_NAME + "(UUID varchar(36) NOT NULL, id_rank int, id_prestige int, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + TOKENS_DB_NAME + "(UUID varchar(36) NOT NULL, Tokens bigint, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + GEMS_DB_NAME + "(UUID varchar(36) NOT NULL, Gems bigint, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_DB_NAME + "(UUID varchar(36) NOT NULL, Blocks bigint, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_WEEKLY_DB_NAME + "(UUID varchar(36) NOT NULL, Blocks bigint, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + MULTIPLIERS_DB_NAME + "(UUID varchar(36) NOT NULL, vote_multiplier double, vote_multiplier_timeleft long, primary key (UUID))");
            execute("CREATE TABLE IF NOT EXISTS " + AUTOMINER_DB_NAME + "(UUID varchar(36) NOT NULL, time int, primary key (UUID))");

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

    public void resetAllTables(CommandSender sender) {
        Schedulers.async().run(() -> {
            for (String table : ALL_TABLES) {
                execute("TRUNCATE " + table);
            }
            sender.sendMessage(Text.colorize("&aUltraPrisonCore - All SQL Tables have been reset."));
        });
    }
}