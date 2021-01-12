package me.drawethree.ultraprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.database.DatabaseCredentials;
import me.drawethree.ultraprisoncore.database.SQLDatabase;


public class MySQLDatabase extends SQLDatabase {

	private final DatabaseCredentials credentials;

	public MySQLDatabase(UltraPrisonCore parent, DatabaseCredentials credentials) {
		super(parent);
		this.credentials = credentials;
		this.connect();
	}


	@Override
	public void connect() {
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
		this.createTables();
	}
}