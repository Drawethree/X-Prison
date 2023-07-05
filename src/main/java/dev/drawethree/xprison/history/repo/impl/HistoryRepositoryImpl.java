package dev.drawethree.xprison.history.repo.impl;

import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.history.model.HistoryLine;
import dev.drawethree.xprison.history.repo.HistoryRepository;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class HistoryRepositoryImpl implements HistoryRepository {

	private static final String TABLE_NAME = "UltraPrison_History";

	private static final String HISTORY_UUID_COLNAME = "uuid";
	private static final String HISTORY_PLAYER_UUID_COLNAME = "player_uuid";
	private static final String HISTORY_MODULE_COLNAME = "module";
	private static final String HISTORY_CONTEXT_COLNAME = "context";
	private static final String HISTORY_CREATED_AT_COLNAME = "created_at";

	private final SQLDatabase database;

	public HistoryRepositoryImpl(SQLDatabase database) {
		this.database = database;
	}

	@Override
	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		List<HistoryLine> returnList = new ArrayList<>();
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"SELECT * FROM " + TABLE_NAME + " where ?=?")) {
			statement.setString(1, HISTORY_PLAYER_UUID_COLNAME);
			statement.setString(2, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				while (set.next()) {

					UUID recordId = UUID.fromString(set.getString(HISTORY_UUID_COLNAME));
					UUID playerUuid = UUID.fromString(set.getString(HISTORY_PLAYER_UUID_COLNAME));
					String moduleName = set.getString(HISTORY_MODULE_COLNAME);
					String context = set.getString(HISTORY_CONTEXT_COLNAME);
					Date createdAt = set.getDate(HISTORY_CREATED_AT_COLNAME);

					HistoryLine line = HistoryLine.builder()
							.uuid(recordId)
							.playerUuid(playerUuid)
							.module(moduleName)
							.context(context)
							.createdAt(createdAt)
							.build();
					returnList.add(line);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnList;
	}

	@Override
	public void addHistoryLine(OfflinePlayer player, HistoryLine history) {
		this.database.executeSqlAsync("INSERT INTO " + TABLE_NAME + " values(?,?,?,?,?)", history.getUuid().toString(), history.getPlayerUuid().toString(), history.getModule(), history.getContext(), history.getCreatedAt());
	}

	@Override
	public void deleteHistory(OfflinePlayer target) {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME + " where ?=?", HISTORY_PLAYER_UUID_COLNAME, target.getUniqueId().toString());
	}

	@Override
	public void createTables() {
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(uuid varchar(36) NOT NULL UNIQUE, player_uuid varchar(36) NOT NULL, module varchar(36) NOT NULL, context TEXT ,created_at DATETIME)");
	}

	@Override
	public void clearTableData() {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME);
	}
}
