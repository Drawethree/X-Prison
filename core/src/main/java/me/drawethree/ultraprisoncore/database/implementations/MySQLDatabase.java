package me.drawethree.ultraprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.database.DatabaseCredentials;
import me.drawethree.ultraprisoncore.database.SQLDatabase;
import me.lucko.helper.Schedulers;

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
		this.createTables();
		this.runSQLUpdates();
	}


	@Override
	public void runSQLUpdates() {
		// v1.4.7-BETA - Added UUID column to UltraPrison_Gangs table as primary key
		Schedulers.async().run(() -> {
			try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='UltraPrison_Gangs' AND column_name ='uuid'")) {
				try (ResultSet set = statement.executeQuery()) {
					if (!set.next()) {
						execute("alter table UltraPrison_Gangs drop primary key", null);
						execute("alter table UltraPrison_Gangs add column uuid varchar(36) not null first", null);
						execute("alter table UltraPrison_Gangs add primary key(uuid,name)", null);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		// v1.5.0 - Renamed multipliers table columns
		Schedulers.async().run(() -> {
			try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='UltraPrison_Multipliers' AND column_name ='vote_multiplier'")) {
				try (ResultSet set = statement.executeQuery()) {
					if (set.next()) {
						execute("alter table UltraPrison_Multipliers rename column vote_multiplier to sell_multiplier", null);
						execute("alter table UltraPrison_Multipliers rename column vote_multiplier_timeleft to sell_multiplier_timeleft", null);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void createTables() {
		Schedulers.async().run(() -> {
			execute("CREATE TABLE IF NOT EXISTS " + RANKS_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, id_rank int, id_prestige bigint, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + TOKENS_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, Tokens bigint, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + GEMS_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, Gems bigint, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + BLOCKS_WEEKLY_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, Blocks bigint, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + MULTIPLIERS_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, sell_multiplier double, sell_multiplier_timeleft long, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + MULTIPLIERS_TOKEN_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, token_multiplier double, token_multiplier_timeleft long, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + AUTOMINER_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, time int, primary key (UUID))");
			execute("CREATE TABLE IF NOT EXISTS " + GANGS_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, name varchar(36) NOT NULL UNIQUE, owner varchar(36) NOT NULL, value int default 0, members text, primary key (UUID,name))");
			execute("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");
		});
	}
}