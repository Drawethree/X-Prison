package dev.drawethree.xprison.database;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.database.model.SQLDatabaseType;
import me.lucko.helper.Schedulers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public abstract class SQLDatabase {

	protected final XPrison plugin;

	SQLDatabase(XPrison plugin) {
		this.plugin = plugin;
	}

	public abstract SQLDatabaseType getDatabaseType();

	public abstract void connect();

	public abstract void close();

	public abstract Connection getConnection();

	public PreparedStatement prepareStatement(Connection connection, String sql, Object... replacements) {

		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			this.replaceQueryParameters(statement,replacements);

			if (this.plugin.isDebugMode()) {
				this.plugin.getLogger().info("Statement prepared: " + sql + " (Replacement values: " + Arrays.toString(replacements) + ")");
			}

			return statement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void executeSql(String sql, Object... replacements) {

		if (sql == null || sql.isEmpty()) {
			return;
		}

		long startTime = System.currentTimeMillis();

		try (Connection c = getConnection(); PreparedStatement statement = prepareStatement(c,sql,replacements)) {

			statement.execute();

			long endTime = System.currentTimeMillis();

			if (this.plugin.isDebugMode()) {
				this.plugin.getLogger().info("Statement executed: " + sql + " (Replacement values: " + Arrays.toString(replacements) + "). Took " + (endTime - startTime) + "ms.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void replaceQueryParameters(PreparedStatement statement, Object[] replacements) {
		if (replacements != null) {
			for (int i = 0; i < replacements.length; i++) {
				int position = i + 1;
				Object value = replacements[i];
				try {
					statement.setObject(position, value);
				} catch (SQLException e) {
					this.plugin.getLogger().warning("Unable to set query parameter at position " + position + " to " + value + " for query: " + statement);
					e.printStackTrace();
				}
			}
		}
	}

	public void executeSqlAsync(String sql, Object... replacements) {
		Schedulers.async().run(() -> this.executeSql(sql, replacements));
	}
}
