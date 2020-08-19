package me.drawethree.wildprisoncore.gangpoints.managers;

import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.drawethree.wildprisoncore.gangpoints.WildPrisonGangPoints;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text.Text;
import net.brcdev.gangs.GangsPlusApi;
import net.brcdev.gangs.event.GangCreateEvent;
import net.brcdev.gangs.gang.Gang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GangPointsManager {


	private WildPrisonGangPoints plugin;
	private HashMap<String, Long> gangPointsCache = new HashMap<>();
	private boolean updating;
	private Task task;
	private LinkedHashMap<String, Long> top10Gangs;

	public GangPointsManager(WildPrisonGangPoints plugin) {
		this.plugin = plugin;

		this.loadGangsDataOnEnable();

		Events.subscribe(GangCreateEvent.class)
				.filter(EventFilters.ignoreCancelled())
				.handler(e -> {
					this.addIntoTable(e.getGang());
					this.gangPointsCache.put(e.getGang().getRawName(), 0L);
				}).bindWith(this.plugin.getCore());

		this.updateTop10();
	}

	private void saveGangData(Gang gang, boolean removeFromCache, boolean async) {
		if (async) {
			Schedulers.async().run(() -> {
				this.plugin.getCore().getSqlDatabase().execute("INSERT IGNORE INTO " + MySQLDatabase.GANG_POINTS_DB_NAME + " VALUES(?,?)", gang.getRawName(), 0);
				this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GANG_POINTS_DB_NAME + " SET " + MySQLDatabase.GANG_POINTS_POINTS_COLNAME + "=? WHERE " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "=?", gangPointsCache.getOrDefault(gang.getRawName(), 0L), gang.getRawName());
				if (removeFromCache) {
					gangPointsCache.remove(gang.getRawName());
				}
				this.plugin.getCore().getLogger().info(String.format("Saved data of gang %s to database.", gang.getRawName()));
			});
		} else {
			this.plugin.getCore().getSqlDatabase().execute("INSERT IGNORE INTO " + MySQLDatabase.GANG_POINTS_DB_NAME + " VALUES(?,?)", gang.getRawName(), 0);
			this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GANG_POINTS_DB_NAME + " SET " + MySQLDatabase.GANG_POINTS_POINTS_COLNAME + "=? WHERE " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "=?", gangPointsCache.getOrDefault(gang.getRawName(), 0L), gang.getRawName());
			if (removeFromCache) {
				gangPointsCache.remove(gang.getRawName());
			}
			this.plugin.getCore().getLogger().info(String.format("Saved data of gang %s to database.", gang.getRawName()));
		}
	}

	public void saveGangsDataOnDisable() {
		this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saving all gangs data");
		Schedulers.sync().run(() -> {

			for (String id : gangPointsCache.keySet()) {
				this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GANG_POINTS_DB_NAME + " SET " + MySQLDatabase.GANG_POINTS_POINTS_COLNAME + "=? WHERE " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "=?", gangPointsCache.get(id), id);
			}

			gangPointsCache.clear();
			this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saved all gangs data to database");
		});
	}

	private void addIntoTable(Gang gang) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getSqlDatabase().execute("DELETE FROM " + MySQLDatabase.GANG_POINTS_DB_NAME + " WHERE " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "=?", gang.getRawName());
			this.plugin.getCore().getSqlDatabase().execute("INSERT IGNORE INTO " + MySQLDatabase.GANG_POINTS_DB_NAME + " VALUES(?,?)", gang.getRawName(), 0);
		});
	}

	private void removeFromTable(Gang gang) {
		Schedulers.async().run(() -> {
			this.plugin.getCore().getSqlDatabase().execute("DELETE FROM " + MySQLDatabase.GANG_POINTS_DB_NAME + " WHERE " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "=?", gang.getRawName());
		});
	}

	private void loadGangsDataOnEnable() {
		GangsPlusApi.getAllGangs().forEach(p -> loadGangData(p));
	}

	private void loadGangData(Gang gang) {
		Schedulers.async().run(() -> {
			try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.GANG_POINTS_DB_NAME + " WHERE " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "=?")) {
				statement.setString(1, gang.getRawName());
				try (ResultSet points = statement.executeQuery()) {
					if (points.next()) {
						gangPointsCache.put(points.getString(MySQLDatabase.GANG_POINTS_UUID_COLNAME), points.getLong(MySQLDatabase.GANG_POINTS_POINTS_COLNAME));
						this.plugin.getCore().getLogger().info(String.format("Loaded data of gang %s from database", gang.getRawName()));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public void setPoints(Gang gang, long newAmount, CommandSender executor) {

		if (gang == null) {
			return;
		}

		gangPointsCache.put(gang.getRawName(), newAmount);

		if (executor != null) {
			executor.sendMessage(Text.colorize(String.format("&e&lGANGS &8» &7You have set %,d Gang Points&7, for &f%,s Gang&7.", newAmount, gang.getName())));
		}
	}

	public void addPoints(Gang gang, long amount, CommandSender executor) {
		if (gang == null) {
			return;
		}

		gangPointsCache.put(gang.getRawName(), gangPointsCache.getOrDefault(gang.getRawName(), 0L) + amount);

		if (executor != null) {
			executor.sendMessage(Text.colorize(String.format("&e&lGANGS &8» &7You have bought &fx%,d Gang Points&7.", amount)));
		}
	}


	public long getGangPoints(Gang gang) {
		if (gang == null) {
			return 0;
		}
		return this.gangPointsCache.getOrDefault(gang.getRawName(), 0L);
	}

	public long getGangPoints(Player player) {
		Gang gang = GangsPlusApi.getPlayersGang(player);
		return this.getGangPoints(gang);
	}


	public void removePoints(Gang gang, long amount, CommandSender executor) {
		if (gang == null) {
			return;
		}

		long currentPoints = gangPointsCache.getOrDefault(gang.getRawName(), 0L);

		if (currentPoints - amount < 0) {
			gangPointsCache.put(gang.getRawName(), 0L);
		} else {
			gangPointsCache.put(gang.getRawName(), currentPoints - amount);
		}

		if (executor != null) {
			executor.sendMessage(Text.colorize(String.format("&e&lGANGS &8» &7You have removed %,d Gang Points&7, from &f%s Gang&7.", amount, gang.getName())));
		}
	}

	public void sendPointsMessage(Player player) {

		Gang gang = GangsPlusApi.getPlayersGang(player);

		if (gang == null) {
			player.sendMessage(Text.colorize("&e&lGANGS &8» &7You have be in a gang to use this command."));
			return;
		}

		player.sendMessage(Text.colorize(String.format("&e&lGANGS &8» &7Your Gang Has: &f%,d Gang Points&7.", this.getGangPoints(player))));
	}

	private void updateTop10() {
		this.updating = true;
		task = Schedulers.async().runRepeating(() -> {
			this.updating = true;
			GangsPlusApi.getAllGangs().forEach(g -> saveGangData(g, false, false));
			this.updateGangTop();
			this.updating = false;
		}, 15, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
	}

	private void updateGangTop() {
		top10Gangs = new LinkedHashMap<>();
		this.plugin.getCore().getLogger().info("Starting updating GangTop");
		try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.GANG_POINTS_UUID_COLNAME + "," + MySQLDatabase.GANG_POINTS_POINTS_COLNAME + " FROM " + MySQLDatabase.GANG_POINTS_DB_NAME + " ORDER BY " + MySQLDatabase.GANG_POINTS_POINTS_COLNAME + " DESC LIMIT 10").executeQuery()) {
			while (set.next()) {
				top10Gangs.put(set.getString(MySQLDatabase.GANG_POINTS_UUID_COLNAME), set.getLong(MySQLDatabase.GANG_POINTS_POINTS_COLNAME));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.plugin.getCore().getLogger().info("GangTop updated!");
	}

	public void stopUpdating() {
		this.plugin.getCore().getLogger().info("Stopping updating Top 10 - Gangs");
		task.close();
	}

	public void sendGangsTop(CommandSender sender) {
		Schedulers.async().run(() -> {
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			if (this.updating) {
				sender.sendMessage(this.plugin.getCore().getTokens().getMessage("top_updating"));
				sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
				return;
			}
			for (int i = 0; i < 10; i++) {
				try {
					String id = (String) top10Gangs.keySet().toArray()[i];

					Optional<Gang> gang = GangsPlusApi.getAllGangs().stream().filter(gang1 -> gang1.getRawName().equalsIgnoreCase(id)).findFirst();

					String name;
					if (!gang.isPresent()) {
						name = "Unknown Gang";
					} else {
						name = gang.get().getName();
					}

					long points = top10Gangs.get(id);
					sender.sendMessage(Text.colorize("&f&l#%position% &e%gang% &8» &7%amount% gang points".replace("%position%", String.valueOf(i + 1)).replace("%gang%", name).replace("%amount%", String.format("%,d", points))));
				} catch (ArrayIndexOutOfBoundsException e) {
					break;
				}
			}
			sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
		});
	}
}
