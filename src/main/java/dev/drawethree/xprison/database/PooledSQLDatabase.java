package dev.drawethree.xprison.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.drawethree.xprison.XPrison;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.drawethree.xprison.utils.log.XPrisonLogger.error;
import static dev.drawethree.xprison.utils.log.XPrisonLogger.info;

public abstract class PooledSQLDatabase extends SQLDatabase {

	protected static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);
	protected HikariDataSource hikari;

	public PooledSQLDatabase(XPrison plugin) {
		super(plugin);
	}

	@Override
	public void close() {
		if (this.hikari != null) {
			this.hikari.close();
			info("&cDatabase Connection closed");
		}
	}

	@Override
	public Connection getConnection() {
		try {
			return this.hikari.getConnection();
		} catch (SQLException e) {
			error("Unable to get database connection!");
			e.printStackTrace();
		}
		return null;
	}


}
