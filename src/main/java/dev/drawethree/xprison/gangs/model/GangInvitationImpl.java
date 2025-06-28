package dev.drawethree.xprison.gangs.model;

import dev.drawethree.xprison.api.gangs.model.Gang;
import dev.drawethree.xprison.api.gangs.model.GangInvitation;
import lombok.Data;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

@Data
public class GangInvitationImpl implements GangInvitation {

	private UUID uuid;
	private GangImpl gangImpl;
	private OfflinePlayer invitedPlayer;
	private OfflinePlayer invitedBy;
	private Date inviteDate;

	public GangInvitationImpl(GangImpl gangImpl, OfflinePlayer invitedPlayer, Player invitedBy) {
		this.uuid = UUID.randomUUID();
		this.gangImpl = gangImpl;
		this.invitedPlayer = invitedPlayer;
		this.invitedBy = invitedBy;
		this.inviteDate = new Date();
	}

	public GangInvitationImpl(UUID uuid, GangImpl gangImpl, OfflinePlayer invitedPlayer, OfflinePlayer invitedBy, Date inviteDate) {
		this.uuid = uuid;
		this.gangImpl = gangImpl;
		this.invitedPlayer = invitedPlayer;
		this.invitedBy = invitedBy;
		this.inviteDate = inviteDate;
	}

	@Override
	public Gang getGang() {
		return gangImpl;
	}
}
