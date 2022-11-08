package dev.drawethree.ultraprisoncore.gangs.repo;

import dev.drawethree.ultraprisoncore.database.Database;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.model.GangInvitation;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GangsRepositoryImpl implements GangsRepository {

	private static final String TABLE_NAME = "UltraPrison_Gangs";
	private static final String INVITES_TABLE_NAME = "UltraPrison_Gang_Invites";
	private static final String GANGS_UUID_COLNAME = "UUID";
	private static final String GANGS_NAME_COLNAME = "name";
	private static final String GANGS_OWNER_COLNAME = "owner";
	private static final String GANGS_MEMBERS_COLNAME = "members";
	private static final String GANGS_VALUE_COLNAME = "value";

	private static final String GANG_INVITATION_UUID = "uuid";
	private static final String GANG_INVITATION_GANG_ID = "gang_id";
	private static final String GANG_INVITATION_INVITED_BY = "invited_by";
	private static final String GANG_INVITATION_INVITED_PLAYER = "invited_player";
	private static final String GANG_INVITATION_INVITE_DATE = "invite_date";

	private final Database database;

	public GangsRepositoryImpl(Database database) {
		this.database = database;
	}

	@Override
	public List<Gang> getAllGangs() {
		List<Gang> returnList = new ArrayList<>();
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + TABLE_NAME); ResultSet set = statement.executeQuery()) {
			while (set.next()) {
				Gang gang = new Gang();

				UUID gangUUID = UUID.fromString(set.getString(GANGS_UUID_COLNAME));
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
	public void createGang(Gang g) {
		this.database.executeSqlAsync("INSERT IGNORE INTO " + UltraPrisonGangs.TABLE_NAME + "(UUID,name,owner,members) VALUES(?,?,?,?)", g.getUuid().toString(), g.getName(), g.getGangOwner().toString(), "");
	}

	@Override
	public void createGangInvitation(GangInvitation gangInvitation) {
		this.database.execute("INSERT IGNORE INTO " + INVITES_TABLE_NAME + "(uuid,gang_id,invited_by,invited_player,invite_date) VALUES(?,?,?,?,?)",
				gangInvitation.getUuid().toString(),
				gangInvitation.getGang().getUuid().toString(),
				gangInvitation.getInvitedBy().getUniqueId().toString(),
				gangInvitation.getInvitedPlayer().getUniqueId().toString(),
				gangInvitation.getInviteDate());
	}

	@Override
	public void deleteGang(Gang g) {
		this.database.executeSqlAsync("DELETE FROM " + TABLE_NAME + " WHERE ?=?", GANGS_UUID_COLNAME, g.getUuid().toString());
		for (GangInvitation gangInvitation : g.getPendingInvites()) {
			this.deleteGangInvitation(gangInvitation);
		}
	}


	@Override
	public List<GangInvitation> getGangInvitations(Gang gang) {
		List<GangInvitation> returnList = new ArrayList<>();
		try (Connection con = this.database.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + INVITES_TABLE_NAME + " WHERE ?=?")) {
			statement.setString(1, GANG_INVITATION_GANG_ID);
			statement.setString(2, gang.getUuid().toString());
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
		this.database.executeSqlAsync("DELETE FROM " + INVITES_TABLE_NAME + " WHERE ?=?", GANG_INVITATION_UUID, gangInvitation.getUuid().toString());
	}

	@Override
	public void updateGang(Gang g) {
		this.database.execute("UPDATE " +
						TABLE_NAME + " SET " +
						GANGS_MEMBERS_COLNAME + "=?," +
						GANGS_NAME_COLNAME + "=?," +
						GANGS_VALUE_COLNAME + "=? WHERE " +
						GANGS_UUID_COLNAME + "=?",
				StringUtils.join(g.getMembersOffline().stream().map(OfflinePlayer::getUniqueId).map(UUID::toString).toArray(), ","),
				g.getName(),
				g.getValue(),
				g.getUuid().toString());

		this.database.execute("DELETE FROM " + INVITES_TABLE_NAME + " WHERE ?=?", GANG_INVITATION_GANG_ID, g.getUuid().toString());

		for (GangInvitation gangInvitation : g.getPendingInvites()) {
			createGangInvitation(gangInvitation);
		}
	}

	@Override
	public void createTables() {
		switch (database.getDatabaseType()) {
			case SQLITE:
			case MYSQL: {
				this.database.executeSqlAsync("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE, name varchar(36) NOT NULL UNIQUE, owner varchar(36) NOT NULL, value int default 0, members text, primary key (UUID,name))");
				this.database.executeSqlAsync("CREATE TABLE IF NOT EXISTS " + INVITES_TABLE_NAME + "(uuid varchar(36) NOT NULL, gang_id varchar(36) NOT NULL, invited_by varchar(36), invited_player varchar(36) not null, invite_date datetime not null, primary key(uuid))");
			}
			default:
				throw new IllegalStateException("Unsupported Database type: " + database.getDatabaseType());
		}
	}
}
