package dev.drawethree.ultraprisoncore.history.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.history.model.HistoryLine;
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
	private static final String INDEX_HISTORY_PLAYER = "idx_history_player";

	private final Database database;

	public HistoryRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		List<HistoryLine> returnList = new ArrayList<>();
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + TABLE_NAME + " where ?=?")) {
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
					z.build();
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
	public void clearHistory(OfflinePlayer target) {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME + " where ?=?", HISTORY_PLAYER_UUID_COLNAME, target.getUniqueId().toString());
	}
}
