package dev.drawethree.ultraprisoncore.multipliers.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.database.impl.MySQLDatabase;
import dev.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import dev.drawethree.ultraprisoncore.multipliers.enums.MultiplierType;
import dev.drawethree.ultraprisoncore.multipliers.multiplier.PlayerMultiplier;
import me.lucko.helper.time.Time;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MultipliersRepositoryImpl implements MultipliersRepository {

	private static final String MULTIPLIERS_UUID_COLNAME = "UUID";
	private static final String MULTIPLIERS_MULTIPLIER_COLNAME = "sell_multiplier";
	private static final String MULTIPLIERS_TIMELEFT_COLNAME = "sell_multiplier_timeleft";

	private static final String MULTIPLIERS_TOKEN_UUID_COLNAME = "UUID";
	private static final String MULTIPLIERS_TOKEN_MULTIPLIER_COLNAME = "token_multiplier";
	private static final String MULTIPLIERS_TOKEN_TIMELEFT_COLNAME = "token_multiplier_timeleft";

	private final Database database;

	public MultipliersRepositoryImpl(Database database) {
		this.database = database;
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
}
