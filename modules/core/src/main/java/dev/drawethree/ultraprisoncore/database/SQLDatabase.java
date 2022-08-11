package dev.drawethree.ultraprisoncore.database;

import com.zaxxer.hikari.HikariDataSource;
import dev.drawethree.ultraprisoncore.UltraPrisonCore;
import dev.drawethree.ultraprisoncore.UltraPrisonModule;
import dev.drawethree.ultraprisoncore.autominer.UltraPrisonAutoMiner;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.model.GangInvitation;
import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.history.UltraPrisonHistory;
import dev.drawethree.ultraprisoncore.history.model.HistoryLine;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import dev.drawethree.ultraprisoncore.ranks.UltraPrisonRanks;
import dev.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SQLDatabase extends Database {

	protected static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);

	protected static final String UUID_PLAYERNAME_TABLE_NAME = "UltraPrison_Nicknames";

	protected static final String RANKS_UUID_COLNAME = "UUID";
	protected static final String RANKS_RANK_COLNAME = "id_rank";

	protected static final String PRESTIGES_UUID_COLNAME = "UUID";
	protected static final String PRESTIGES_PRESTIGE_COLNAME = "id_prestige";

	protected static final String TOKENS_UUID_COLNAME = "UUID";
	protected static final String TOKENS_TOKENS_COLNAME = "Tokens";

	protected static final String GEMS_UUID_COLNAME = "UUID";
	protected static final String GEMS_GEMS_COLNAME = "Gems";

	protected static final String BLOCKS_UUID_COLNAME = "UUID";
	protected static final String BLOCKS_BLOCKS_COLNAME = "Blocks";

	protected static final String MULTIPLIERS_UUID_COLNAME = "UUID";
	protected static final String MULTIPLIERS_MULTIPLIER_COLNAME = "sell_multiplier";
	protected static final String MULTIPLIERS_TIMELEFT_COLNAME = "sell_multiplier_timeleft";

	protected static final String MULTIPLIERS_TOKEN_UUID_COLNAME = "UUID";
	protected static final String MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME = "token_multiplier";
	protected static final String MULTIPLIERS_TOKEN_TIMELEFT_COLNAME = "token_multiplier_timeleft";

	protected static final String UUID_PLAYERNAME_UUID_COLNAME = "UUID";
	protected static final String UUID_PLAYERNAME_NICK_COLNAME = "nickname";

	protected static final String AUTOMINER_UUID_COLNAME = "UUID";
	protected static final String AUTOMINER_TIME_COLNAME = "time";

	protected static final String GANGS_UUID_COLNAME = "UUID";
	protected static final String GANGS_NAME_COLNAME = "name";
	protected static final String GANGS_OWNER_COLNAME = "owner";
	protected static final String GANGS_MEMBERS_COLNAME = "members";
	protected static final String GANGS_VALUE_COLNAME = "value";

	protected static final String GANG_INVITATION_UUID = "uuid";
	protected static final String GANG_INVITATION_GANG_ID = "gang_id";
	protected static final String GANG_INVITATION_INVITED_BY = "invited_by";
	protected static final String GANG_INVITATION_INVITED_PLAYER = "invited_player";
	protected static final String GANG_INVITATION_INVITE_DATE = "invite_date";

	protected static final String HISTORY_UUID_COLNAME = "uuid";
	protected static final String HISTORY_PLAYER_UUID_COLNAME = "player_uuid";
	protected static final String HISTORY_MODULE_COLNAME = "module";
	protected static final String HISTORY_CONTEXT_COLNAME = "context";
	protected static final String HISTORY_CREATED_AT_COLNAME = "created_at";

	protected static final String INDEX_HISTORY_PLAYER = "idx_history_player";

	protected UltraPrisonCore plugin;
	protected HikariDataSource hikari;

	public SQLDatabase(UltraPrisonCore plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void createIndexes() {
		this.executeAsync(String.format("CREATE INDEX %s ON %s (%s)", INDEX_HISTORY_PLAYER, UltraPrisonHistory.TABLE_NAME, HISTORY_PLAYER_UUID_COLNAME));
	}

	public synchronized void execute(String sql, Object... replacements) {

		if (sql == null || sql.isEmpty()) {
			return;
		}

		try (Connection c = this.hikari.getConnection(); PreparedStatement statement = c.prepareStatement(sql)) {
			if (replacements != null) {
				for (int i = 0; i < replacements.length; i++) {
					statement.setObject(i + 1, replacements[i]);
				}
			}
			if (this.plugin.isDebugMode()) {
				this.plugin.getLogger().info("Executing statement: " + sql + " (Replacement values: " + Arrays.toString(replacements) + ")");
			}
			statement.execute();
		} catch (SQLException e) {
			if (e.getErrorCode() != 1061) {
				e.printStackTrace();
			}
		}
	}


	public void executeAsync(String sql, Object... replacements) {
		Schedulers.async().run(() -> {
			this.execute(sql, replacements);
		});
	}


	public void close() {
		if (this.hikari != null) {
			this.hikari.close();
			this.plugin.getLogger().info("Closing SQL Connection");
		}
	}

	@Override
	public boolean resetAllData() {
		Schedulers.async().run(() -> {
			for (UltraPrisonModule module : this.plugin.getModules()) {
				this.resetData(module);
			}
		});
		return true;
	}

	@Override
	public boolean resetData(UltraPrisonModule module) {
		Schedulers.async().run(() -> {
			for (String table : module.getTables()) {
				execute("TRUNCATE TABLE " + table);

			}
		});
		return true;
	}

	@Override
	public void updatePlayerNickname(OfflinePlayer player) {
		this.executeAsync("INSERT INTO " + MySQLDatabase.UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.UUID_PLAYERNAME_NICK_COLNAME + "=?", player.getUniqueId().toString(), player.getName(), player.getName());
	}

	@Override
	public long getPlayerTokens(OfflinePlayer p) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonTokens.TABLE_NAME_TOKENS + " WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.TOKENS_TOKENS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateTokens(OfflinePlayer p, long amount) {
		this.executeAsync("UPDATE " + UltraPrisonTokens.TABLE_NAME_TOKENS + " SET " + MySQLDatabase.TOKENS_TOKENS_COLNAME + "=? WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", amount, p.getUniqueId().toString());
	}

	@Override
	public void resetBlocksWeekly(CommandSender sender) {
		this.execute("DELETE FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY);
	}

	@Override
	public long getPlayerGems(OfflinePlayer p) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonGems.TABLE_NAME + " WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.GEMS_GEMS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateGems(OfflinePlayer p, long newAmount) {
		this.execute("UPDATE " + UltraPrisonGems.TABLE_NAME + " SET " + MySQLDatabase.GEMS_GEMS_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", newAmount, p.getUniqueId().toString());
	}

	@Override
	public void updateRank(OfflinePlayer player, int newRank) {
		this.execute("UPDATE " + UltraPrisonRanks.TABLE_NAME + " SET " + MySQLDatabase.RANKS_RANK_COLNAME + "=? WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?", newRank, player.getUniqueId().toString());
	}

	@Override
	public void updatePrestige(OfflinePlayer player, long newPrestige) {
		this.execute("UPDATE " + UltraPrisonPrestiges.TABLE_NAME + " SET " + MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME + "=? WHERE " + MySQLDatabase.PRESTIGES_UUID_COLNAME + "=?", newPrestige, player.getUniqueId().toString());
	}

	@Override
	public int getPlayerRank(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonRanks.TABLE_NAME + " WHERE " + MySQLDatabase.RANKS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getInt(SQLDatabase.RANKS_RANK_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public long getPlayerPrestige(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonPrestiges.TABLE_NAME + " WHERE " + MySQLDatabase.PRESTIGES_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(SQLDatabase.PRESTIGES_PRESTIGE_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void removeExpiredAutoMiners() {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM " + UltraPrisonAutoMiner.TABLE_NAME + " WHERE " + MySQLDatabase.AUTOMINER_TIME_COLNAME + " <= 0")) {
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getPlayerAutoMinerTime(OfflinePlayer p) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonAutoMiner.TABLE_NAME + " WHERE " + MySQLDatabase.AUTOMINER_UUID_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getInt(MySQLDatabase.AUTOMINER_TIME_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public PlayerMultiplier getSellMultiplier(Player player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonMultipliers.TABLE_NAME + " WHERE " + MySQLDatabase.MULTIPLIERS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					double multiplier = set.getDouble(MySQLDatabase.MULTIPLIERS_MULTIPLIER_COLNAME);
					long endTime = set.getLong(MySQLDatabase.MULTIPLIERS_TIMELEFT_COLNAME);
					if (endTime > Time.nowMillis()) {
						return new PlayerMultiplier(player.getUniqueId(), multiplier, endTime, MultiplierType.SELL);
					}
				}
			}
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not load sell multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PlayerMultiplier getTokenMultiplier(Player player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonMultipliers.TABLE_NAME_TOKEN + " WHERE " + MySQLDatabase.MULTIPLIERS_TOKEN_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					double multiplier = set.getDouble(MySQLDatabase.MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME);
					long endTime = set.getLong(MySQLDatabase.MULTIPLIERS_TOKEN_TIMELEFT_COLNAME);
					if (endTime > Time.nowMillis()) {
						return new PlayerMultiplier(player.getUniqueId(), multiplier, endTime, MultiplierType.TOKENS);
					}
				}
			}
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not load token multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void removeExpiredMultipliers() {
		//Sell multipliers
		try (Connection con = this.hikari.getConnection();
			 PreparedStatement statement = con.prepareStatement("DELETE FROM " + UltraPrisonMultipliers.TABLE_NAME + " WHERE " + MySQLDatabase.MULTIPLIERS_TIMELEFT_COLNAME + " < " + Time.nowMillis());
			 PreparedStatement statement2 = con.prepareStatement("DELETE FROM " + UltraPrisonMultipliers.TABLE_NAME_TOKEN + " WHERE " + MySQLDatabase.MULTIPLIERS_TOKEN_TIMELEFT_COLNAME + " < " + Time.nowMillis())) {
			statement.execute();
			statement2.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getPlayerBrokenBlocks(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public long getPlayerBrokenBlocksWeekly(OfflinePlayer player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					return set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void updateBlocks(OfflinePlayer player, long newAmount) {
		this.execute("UPDATE " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " SET " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + "=? WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", newAmount, player.getUniqueId().toString());
	}

	@Override
	public void updateBlocksWeekly(OfflinePlayer player, long newAmount) {
		this.execute("UPDATE " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " SET " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + "=? WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", newAmount, player.getUniqueId().toString());
	}

	@Override
	public void saveSellMultiplier(Player player, PlayerMultiplier multiplier) {

		if (multiplier == null || !multiplier.isValid()) {
			this.deleteSellMultiplier(player);
			return;
		}

		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + UltraPrisonMultipliers.TABLE_NAME + " VALUES(?,?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.MULTIPLIERS_MULTIPLIER_COLNAME + "=?, " + MySQLDatabase.MULTIPLIERS_TIMELEFT_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.setDouble(2, multiplier.getMultiplier());
			statement.setLong(3, multiplier.getEndTime());
			statement.setDouble(4, multiplier.getMultiplier());
			statement.setLong(5, multiplier.getEndTime());
			statement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not save sell multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
	}

	@Override
	public void deleteSellMultiplier(Player player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM " + UltraPrisonMultipliers.TABLE_NAME + " WHERE " + MySQLDatabase.MULTIPLIERS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not delete sell multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
	}

	@Override
	public void deleteTokenMultiplier(Player player) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("DELETE FROM " + UltraPrisonMultipliers.TABLE_NAME_TOKEN + " WHERE " + MySQLDatabase.MULTIPLIERS_UUID_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not delete token multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
	}

	@Override
	public void saveTokenMultiplier(Player player, PlayerMultiplier multiplier) {

		if (multiplier == null || !multiplier.isValid()) {
			this.deleteTokenMultiplier(player);
			return;
		}

		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + UltraPrisonMultipliers.TABLE_NAME_TOKEN + " VALUES(?,?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME + "=?, " + MySQLDatabase.MULTIPLIERS_TOKEN_TIMELEFT_COLNAME + "=?")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.setDouble(2, multiplier.getMultiplier());
			statement.setLong(3, multiplier.getEndTime());
			statement.setDouble(4, multiplier.getMultiplier());
			statement.setLong(5, multiplier.getEndTime());
			statement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not save token multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
	}

	@Override
	public void saveAutoMiner(Player p, int timeLeft) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO " + UltraPrisonAutoMiner.TABLE_NAME + " VALUES (?,?) ON DUPLICATE KEY UPDATE " + MySQLDatabase.AUTOMINER_TIME_COLNAME + "=?")) {
			statement.setString(1, p.getUniqueId().toString());
			statement.setInt(2, timeLeft);
			statement.setInt(3, timeLeft);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<UUID, Integer> getTop10Prestiges() {
		Map<UUID, Integer> top10Prestige = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.PRESTIGES_UUID_COLNAME + "," + MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME + " FROM " + UltraPrisonPrestiges.TABLE_NAME + " ORDER BY " + MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME + " DESC LIMIT 10").executeQuery()) {
			while (set.next()) {
				top10Prestige.put(UUID.fromString(set.getString(MySQLDatabase.PRESTIGES_UUID_COLNAME)), set.getInt(MySQLDatabase.PRESTIGES_PRESTIGE_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return top10Prestige;
	}

	@Override
	public Map<UUID, Long> getTopGems(int amountOfRecords) {
		Map<UUID, Long> topGems = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.GEMS_UUID_COLNAME + "," + MySQLDatabase.GEMS_GEMS_COLNAME + " FROM " + UltraPrisonGems.TABLE_NAME + " ORDER BY " + MySQLDatabase.GEMS_GEMS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				topGems.put(UUID.fromString(set.getString(MySQLDatabase.GEMS_UUID_COLNAME)), set.getLong(MySQLDatabase.GEMS_GEMS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topGems;
	}

	@Override
	public Map<UUID, Long> getTopBlocksWeekly(int amountOfRecords) {
		Map<UUID, Long> topBlocksWeekly = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.BLOCKS_UUID_COLNAME + "," + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " ORDER BY " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				topBlocksWeekly.put(UUID.fromString(set.getString(MySQLDatabase.BLOCKS_UUID_COLNAME)), set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topBlocksWeekly;
	}

	@Override
	public Map<UUID, Long> getTopTokens(int amountOfRecords) {
		Map<UUID, Long> top10Tokens = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.TOKENS_UUID_COLNAME + "," + MySQLDatabase.TOKENS_TOKENS_COLNAME + " FROM " + UltraPrisonTokens.TABLE_NAME_TOKENS + " ORDER BY " + MySQLDatabase.TOKENS_TOKENS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				top10Tokens.put(UUID.fromString(set.getString(MySQLDatabase.TOKENS_UUID_COLNAME)), set.getLong(MySQLDatabase.TOKENS_TOKENS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return top10Tokens;
	}

	@Override
	public Map<UUID, Long> getTopBlocks(int amountOfRecords) {
		Map<UUID, Long> topBlocks = new LinkedHashMap<>();
		try (Connection con = this.hikari.getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.BLOCKS_UUID_COLNAME + "," + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " FROM " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " ORDER BY " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " DESC LIMIT " + amountOfRecords).executeQuery()) {
			while (set.next()) {
				topBlocks.put(UUID.fromString(set.getString(MySQLDatabase.BLOCKS_UUID_COLNAME)), set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topBlocks;
	}

	@Override
	public void addIntoTokens(OfflinePlayer player, long startingTokens) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_TOKENS + " VALUES(?,?)", player.getUniqueId().toString(), startingTokens);
	}

	@Override
	public void addIntoRanks(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonRanks.TABLE_NAME + "(UUID,id_rank) VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoPrestiges(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonPrestiges.TABLE_NAME + "(UUID,id_prestige) VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoBlocks(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_BLOCKS + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoBlocksWeekly(OfflinePlayer player) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonTokens.TABLE_NAME_BLOCKS_WEEKLY + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoGems(OfflinePlayer player, long startingGems) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonGems.TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), startingGems);
	}

	@Override
	public List<Gang> getAllGangs() {
		List<Gang> returnList = new ArrayList<>();
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonGangs.TABLE_NAME, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE); ResultSet set = statement.executeQuery()) {
			while (set.next()) {
				Gang gang = new Gang();

				UUID gangUUID;
				try {
					gangUUID = UUID.fromString(set.getString(GANGS_UUID_COLNAME));
				} catch (Exception e) {
					gangUUID = UUID.randomUUID();
					set.updateString(GANGS_UUID_COLNAME, gangUUID.toString());
					set.updateRow();
				}

				gang.setUuid(gangUUID);

				String gangName = set.getString(GANGS_NAME_COLNAME);
				gang.setName(gangName);
				UUID owner = UUID.fromString(set.getString(GANGS_OWNER_COLNAME));
				gang.setGangOwner(owner);
				List<UUID> members = new ArrayList<>();

				for (String s : set.getString(GANGS_MEMBERS_COLNAME).split(",")) {
					if (s.isEmpty()) {
						continue;
					}
					try {
						UUID uuid = UUID.fromString(s);
						members.add(uuid);
					} catch (Exception e) {
						Log.warn("Unable to fetch UUID: " + s);
						e.printStackTrace();
					}
				}
				gang.setGangMembers(members);

				int value = set.getInt(GANGS_VALUE_COLNAME);
				gang.setValue(value);
				List<GangInvitation> gangInvitations = getGangInvitations(gang);
				gang.setPendingInvites(gangInvitations);

				returnList.add(gang);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnList;
	}

	@Override
	public List<GangInvitation> getGangInvitations(Gang gang) {
		List<GangInvitation> returnList = new ArrayList<>();
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonGangs.INVITES_TABLE_NAME + " WHERE gang_id=?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
			statement.setString(1, gang.getUuid().toString());
			try (ResultSet set = statement.executeQuery()) {
				while (set.next()) {
					UUID uuid = UUID.fromString(set.getString(GANG_INVITATION_UUID));
					OfflinePlayer invitedPlayer = Players.getOfflineNullable(UUID.fromString(set.getString(GANG_INVITATION_INVITED_PLAYER)));
					OfflinePlayer invitedBy = Players.getOfflineNullable(UUID.fromString(set.getString(GANG_INVITATION_INVITED_BY)));
					Date inviteDate = set.getDate(GANG_INVITATION_INVITE_DATE);
					GangInvitation invitation = new GangInvitation(uuid, gang, invitedPlayer, invitedBy, inviteDate);
					returnList.add(invitation);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnList;
	}

	@Override
	public void deleteGangInvitation(GangInvitation gangInvitation) {
		this.executeAsync("DELETE FROM " + UltraPrisonGangs.INVITES_TABLE_NAME + " WHERE uuid=?", gangInvitation.getUuid().toString());
	}

	@Override
	public void updateGang(Gang g) {
		this.execute("UPDATE " +
						UltraPrisonGangs.TABLE_NAME + " SET " +
						MySQLDatabase.GANGS_MEMBERS_COLNAME + "=?," +
						MySQLDatabase.GANGS_NAME_COLNAME + "=?," +
						MySQLDatabase.GANGS_VALUE_COLNAME + "=? WHERE " +
						MySQLDatabase.GANGS_UUID_COLNAME + "=?",
				StringUtils.join(g.getMembersOffline().stream().map(OfflinePlayer::getUniqueId).map(UUID::toString).toArray(), ","),
				g.getName(),
				g.getValue(),
				g.getUuid().toString());

		this.execute("DELETE FROM " + UltraPrisonGangs.INVITES_TABLE_NAME + " WHERE gang_id=?", g.getUuid().toString());

		for (GangInvitation gangInvitation : g.getPendingInvites()) {
			createGangInvitation(gangInvitation);
		}

	}

	@Override
	public void createGang(Gang g) {
		this.executeAsync("INSERT IGNORE INTO " + UltraPrisonGangs.TABLE_NAME + "(UUID,name,owner,members) VALUES(?,?,?,?)", g.getUuid().toString(), g.getName(), g.getGangOwner().toString(), "");
	}

	@Override
	public void createGangInvitation(GangInvitation gangInvitation) {
		this.execute("INSERT IGNORE INTO " + UltraPrisonGangs.INVITES_TABLE_NAME + "(uuid,gang_id,invited_by,invited_player,invite_date) VALUES(?,?,?,?,?)",
				gangInvitation.getUuid().toString(),
				gangInvitation.getGang().getUuid().toString(),
				gangInvitation.getInvitedBy().getUniqueId().toString(),
				gangInvitation.getInvitedPlayer().getUniqueId().toString(),
				gangInvitation.getInviteDate());
	}

	@Override
	public void deleteGang(Gang g) {
		this.executeAsync("DELETE FROM " + UltraPrisonGangs.TABLE_NAME + " WHERE UUID=?", g.getUuid().toString());
		for (GangInvitation gangInvitation : g.getPendingInvites()) {
			this.deleteGangInvitation(gangInvitation);
		}
	}

	@Override
	public List<HistoryLine> getPlayerHistory(OfflinePlayer player) {
		List<HistoryLine> returnList = new ArrayList<>();
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + UltraPrisonHistory.TABLE_NAME + " where player_uuid=?")) {
			statement.setString(1, player.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				while (set.next()) {
					UUID recordId = UUID.fromString(set.getString(HISTORY_UUID_COLNAME));
					UUID playerUuid = UUID.fromString(set.getString(HISTORY_PLAYER_UUID_COLNAME));
					String moduleName = set.getString(HISTORY_MODULE_COLNAME);
					String context = set.getString(HISTORY_CONTEXT_COLNAME);
					Date createdAt = set.getDate(HISTORY_CREATED_AT_COLNAME);

					HistoryLine line = new HistoryLine(recordId, playerUuid, moduleName, context, createdAt);
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
		this.executeAsync("INSERT INTO " + UltraPrisonHistory.TABLE_NAME + " values(?,?,?,?,?)", history.getUuid().toString(), history.getPlayerUuid().toString(), history.getModule(), history.getContext(), history.getCreatedAt());
	}

	@Override
	public void clearHistory(OfflinePlayer target) {
		this.executeAsync("DELETE FROM " + UltraPrisonHistory.TABLE_NAME + " where player_uuid=?", target.getUniqueId().toString());
	}
}
