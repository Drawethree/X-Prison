package me.drawethree.ultraprisoncore.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.database.SQLDatabase;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import me.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Log;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SQLiteDatabase extends SQLDatabase {

	private static final String FILE_NAME = "playerdata.db";
	private String filePath;

	public SQLiteDatabase(UltraPrisonCore plugin) {
		super(plugin);

		this.plugin.getLogger().info("Using SQLite (local) database.");

		this.filePath = this.plugin.getDataFolder().getPath() + File.separator + FILE_NAME;
		this.plugin.getLogger().info(String.format("Path to SQLite Database %s is %s", FILE_NAME, this.filePath));
		this.createDBFile();

		this.connect();
	}

	@Override
	public void connect() {

		final HikariConfig hikari = new HikariConfig();

		hikari.setPoolName("ultraprison-1");

		hikari.setDriverClassName("org.sqlite.JDBC");
		hikari.setJdbcUrl("jdbc:sqlite:" + this.filePath);
		hikari.setConnectionTestQuery("SELECT 1");

		hikari.setMinimumIdle(MINIMUM_IDLE);
		hikari.setMaxLifetime(MAX_LIFETIME);
		hikari.setConnectionTimeout(0);
		hikari.setMaximumPoolSize(1);
		hikari.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));

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
			execute("CREATE TABLE IF NOT EXISTS " + GANGS_TABLE_NAME + "(name varchar(36) NOT NULL UNIQUE, owner varchar(36) NOT NULL, value int default 0, members text, primary key (name))");
			execute("CREATE TABLE IF NOT EXISTS " + UUID_PLAYERNAME_TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, nickname varchar(16) NOT NULL, primary key (UUID))");

			// v1.4.7-BETA - Added UUID column to UltraPrison_Gangs table
			try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM UltraPrison_Gangs"); ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					try {
						set.findColumn("uuid");
					} catch (SQLException e) {
						execute("alter table UltraPrison_Gangs add column uuid varchar(36) not null", null);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// v1.5.0 - Renamed multipliers table columns
			Schedulers.async().run(() -> {
				try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM UltraPrison_Multipliers"); ResultSet set = statement.executeQuery()) {
					if (set.next()) {
						try {
							set.findColumn("sell_multiplier");
						} catch (Exception e) {
							execute("alter table UltraPrison_Multipliers rename column vote_multiplier to sell_multiplier", null);
							execute("alter table UltraPrison_Multipliers rename column vote_multiplier_timeleft to sell_multiplier_timeleft", null);
						}

					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		});
	}


	@Override
	public void runSQLUpdates() {
	}

	@Override
	public void addIntoTokens(OfflinePlayer player) {
		this.execute("INSERT OR IGNORE INTO " + MySQLDatabase.TOKENS_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoRanksAndPrestiges(OfflinePlayer player) {
		this.execute("INSERT OR IGNORE INTO " + MySQLDatabase.RANKS_TABLE_NAME + " VALUES(?,?,?)", player.getUniqueId().toString(), 0, 0);
	}

	@Override
	public void addIntoBlocks(OfflinePlayer player) {
		this.execute("INSERT OR IGNORE INTO " + MySQLDatabase.BLOCKS_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoBlocksWeekly(OfflinePlayer player) {
		this.execute("INSERT OR IGNORE INTO " + MySQLDatabase.BLOCKS_WEEKLY_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void addIntoGems(OfflinePlayer player) {
		this.execute("INSERT OR IGNORE INTO " + MySQLDatabase.GEMS_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), 0);
	}

	@Override
	public void createGang(Gang g) {
		this.execute("INSERT OR IGNORE INTO " + MySQLDatabase.GANGS_TABLE_NAME + "(name,owner,members) VALUES(?,?,?)", g.getName(), g.getGangOwner().toString(), "");
	}

	@Override
	public void saveAutoMiner(Player p, int timeLeft) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + MySQLDatabase.AUTOMINER_TABLE_NAME + " VALUES (?,?) ")) {
			statement.setString(1, p.getUniqueId().toString());
			statement.setInt(2, timeLeft);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveSellMultiplier(Player player, PlayerMultiplier multiplier) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + MySQLDatabase.MULTIPLIERS_TABLE_NAME + " VALUES(?,?,?)")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.setDouble(2, multiplier.getMultiplier());
			statement.setLong(3, multiplier.getEndTime());
			statement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not save sell multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
	}

	@Override
	public void saveTokenMultiplier(Player player, PlayerMultiplier multiplier) {
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT OR REPLACE INTO " + MySQLDatabase.MULTIPLIERS_TOKEN_TABLE_NAME + " VALUES(?,?,?)")) {
			statement.setString(1, player.getUniqueId().toString());
			statement.setDouble(2, multiplier.getMultiplier());
			statement.setLong(3, multiplier.getEndTime());
			statement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warning("Could not save token multiplier for player " + player.getName() + "!");
			e.printStackTrace();
		}
	}

	@Override
	public void updatePlayerNickname(OfflinePlayer player) {
		this.executeAsync("INSERT OR REPLACE INTO " + MySQLDatabase.UUID_PLAYERNAME_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), player.getName());
	}

	@Override
	public List<Gang> getAllGangs() {
		List<Gang> returnList = new ArrayList<>();
		try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.GANGS_TABLE_NAME)) {
			try (ResultSet set = statement.executeQuery()) {
				while (set.next()) {

					String gangName = set.getString(GANGS_NAME_COLNAME);
					UUID gangUUID;

					try {
						gangUUID = UUID.fromString(set.getString(GANGS_UUID_COLNAME));
					} catch (Exception e) {
						gangUUID = UUID.randomUUID();
						try (PreparedStatement statement1 = con.prepareStatement("UPDATE " + GANGS_TABLE_NAME + " SET " + GANGS_UUID_COLNAME + "=? WHERE " + GANGS_NAME_COLNAME + "=?")) {
							statement1.setString(1, gangUUID.toString());
							statement1.setString(2, gangName);
							statement1.execute();
						}
					}

					UUID owner = UUID.fromString(set.getString(GANGS_OWNER_COLNAME));

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

					int value = set.getInt(GANGS_VALUE_COLNAME);
					returnList.add(new Gang(gangUUID, gangName, owner, members, value));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnList;
	}

}
