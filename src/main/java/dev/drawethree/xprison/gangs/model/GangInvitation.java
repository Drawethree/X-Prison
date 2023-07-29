package dev.drawethree.xprison.gangs.model;

import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

@Data
public class GangInvitation {

	private UUID uuid;
	private Gang gang;
	private OfflinePlayer invitedPlayer;
	private OfflinePlayer invitedBy;
	private Date inviteDate;

	public GangInvitation(Gang gang, OfflinePlayer invitedPlayer, Player invitedBy) {
		this.uuid = UUID.randomUUID();
		this.gang = gang;
		this.invitedPlayer = invitedPlayer;
		this.invitedBy = invitedBy;
		this.inviteDate = new Date();
	}

	public GangInvitation(UUID uuid, Gang gang, OfflinePlayer invitedPlayer, OfflinePlayer invitedBy, Date inviteDate) {
		this.uuid = uuid;
		this.gang = gang;
		this.invitedPlayer = invitedPlayer;
		this.invitedBy = invitedBy;
		this.inviteDate = inviteDate;
	}
}
