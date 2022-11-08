package dev.drawethree.ultraprisoncore.database;

import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.database.model.SQLDatabaseType;
import me.lucko.helper.Schedulers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public abstract class SQLDatabase {

	protected final UltraPrisonCore plugin;

	SQLDatabase(UltraPrisonCore plugin) {
		this.plugin = plugin;
	}

	public abstract SQLDatabaseType getDatabaseType();

	public abstract void connect();

	public abstract void close();

	public abstract Connection getConnection();

	public void executeSql(String sql, Object... replacements) {

		if (sql == null || sql.isEmpty()) {
			return;
		}

		long startTime = System.currentTimeMillis();
		try (Connection c = getConnection(); PreparedStatement statement = c.prepareStatement(sql)) {

			this.replaceQueryParamaters(statement, replacements);

			statement.execute();

			long endTime = System.currentTimeMillis();

			if (this.plugin.isDebugMode()) {
				this.plugin.getLogger().info("Statement executed: " + sql + " (Replacement values: " + Arrays.toString(replacements) + "). Took " + (endTime - startTime) + "ms.");
			}

		} catch (SQLException e) {
			if (e.getErrorCode() != 1061) {
				e.printStackTrace();
			}
		}
	}

	private void replaceQueryParamaters(PreparedStatement statement, Object[] replacements) {
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

	@Override
	public boolean resetAllData() {
		for (UltraPrisonModule module : this.plugin.getModules()) {
			this.resetData(module);
		}
		return true;
	}

	@Override
	public boolean resetData(UltraPrisonModule module) {
		for (String table : module.getTables()) {
			executeSqlAsync("TRUNCATE TABLE " + table);
		}
		return true;
	}
}
