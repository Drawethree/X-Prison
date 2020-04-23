package me.drawethree.wildprisontokens.database;

import lombok.Getter;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.Schedulers;

import java.sql.*;


public class MySQLDatabase {


    public static final String TOKENS_DB_NAME = "WildPrison_Tokens";
    public static final String BLOCKS_DB_NAME = "WildPrison_BlocksBroken";

    public static final String TOKENS_UUID_COLNAME = "UUID";
    public static final String TOKENS_TOKENS_COLNAME = "Tokens";

    public static final String BLOCKS_UUID_COLNAME = "UUID";
    public static final String BLOCKS_BLOCKS_COLNAME = "Blocks";

    @Getter
    private WildPrisonTokens parent;

    private Connection connection;
    private DatabaseCredentials credentials;

    public MySQLDatabase(WildPrisonTokens parent) {
        this.parent = parent;
        this.credentials = DatabaseCredentials.fromConfig(parent.getConfig());
        this.connect();
    }


    private synchronized void connect() {
        Schedulers.async().run(() -> {
            if (this.parent != null && !this.parent.isEnabled()) {
                return;
            }

            try {
                openConnection();
                createTables();
            } catch (Exception e) {
                e.printStackTrace();
                this.parent.getServer().getPluginManager().disablePlugin(this.parent);
            }
        });
    }

    private synchronized void openConnection() throws SQLException {
        this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + this.credentials.getHost() + ":" + this.credentials.getPort() + "/" + this.credentials.getDatabaseName(), this.credentials.getUserName(), this.credentials.getPassword());

    }

    //Always call async!
    public synchronized ResultSet query(String sql, Object... replacements) {

        if (this.parent != null && !parent.isEnabled()) {
            return null;
        }

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
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

    }

    private synchronized void createTables() {
        Schedulers.async().run(() -> execute("CREATE TABLE IF NOT EXISTS " + TOKENS_DB_NAME + "(UUID varchar(36) NOT NULL, Tokens long, primary key (UUID))"));
        Schedulers.async().run(() -> execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_DB_NAME + "(UUID varchar(36) NOT NULL, Blocks long, primary key (UUID))"));
    }


    //Always execute async!
    public synchronized void execute(String sql, Object... replacements) {

        if (this.parent != null && !parent.isEnabled()) {
            return;
        }

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
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

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.credentials.getHost() + ":" + this.credentials.getPort() + "/" + this.credentials.getDatabaseName(), this.credentials.getUserName(), this.credentials.getPassword());
        }
        return connection;
    }
}