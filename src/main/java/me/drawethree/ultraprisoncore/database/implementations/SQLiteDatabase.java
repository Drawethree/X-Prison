package me.drawethree.ultraprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.database.SQLDatabase;

import java.io.File;
import java.io.IOException;

public class SQLiteDatabase extends SQLDatabase {

	private static final String FILE_NAME = "playerdata.db";
	private String filePath;

	public SQLiteDatabase(UltraPrisonCore plugin) {
		super(plugin);

		this.filePath = this.plugin.getDataFolder().getPath() + File.pathSeparator + FILE_NAME;
		this.plugin.getLogger().info(String.format("Path to %s is %s", FILE_NAME, this.filePath));
		this.createDBFile();

		this.connect();
	}

	@Override
	public void connect() {

		final HikariConfig hikari = new HikariConfig();

		hikari.setPoolName("ultraprison-" + POOL_COUNTER.getAndIncrement());

		hikari.setJdbcUrl("jdbc:sqlite:" + this.filePath);

		hikari.setMinimumIdle(MINIMUM_IDLE);
		hikari.setMaxLifetime(MAX_LIFETIME);
		hikari.setConnectionTimeout(CONNECTION_TIMEOUT);
		hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
		hikari.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

		this.hikari = new HikariDataSource(hikari);
		this.createTables();
	}

	private void createDBFile() {
		File yourFile = new File(this.filePath);
		try {
			yourFile.createNewFile();
		} catch (IOException e) {
			this.plugin.getLogger().warning(String.format("Unable to create %s", FILE_NAME));
			e.printStackTrace();
		}
	}

}
