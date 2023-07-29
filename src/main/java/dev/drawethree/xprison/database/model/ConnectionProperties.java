package dev.drawethree.xprison.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@AllArgsConstructor
public class ConnectionProperties {

	private final long idleTimeout, maxLifetime, connectionTimeout, leakDetectionThreshold, keepAliveTime;
	private final int minimumIdle, maximumPoolSize;
	private final String testQuery, characterEncoding;

	public static ConnectionProperties fromConfig(FileConfiguration config) {

		String rootPath = "connection_properties.";

		long connectionTimeout = config.getLong(rootPath + "connection_timeout");
		long idleTimeout = config.getLong(rootPath + "idle_timeout");
		long keepAliveTime = config.getLong(rootPath + "keep_alive_time");
		long maxLifeTime = config.getLong(rootPath + "max_life_time");
		int minimumIdle = config.getInt(rootPath + "minimum_idle");
		int maximumPoolSize = config.getInt(rootPath + "maximum_pool_size");
		long leakDetectionThreshold = config.getLong(rootPath + "leak_detection_threshold");
		String characterEncoding = config.getString(rootPath + "character_encoding", "utf8");
		String testQuery = config.getString(rootPath + "connection_test_query");
		return new ConnectionProperties(idleTimeout, maxLifeTime, connectionTimeout, leakDetectionThreshold, keepAliveTime, minimumIdle, maximumPoolSize, testQuery,characterEncoding);
	}
}

