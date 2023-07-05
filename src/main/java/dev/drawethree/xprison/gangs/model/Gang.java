package dev.drawethree.xprison.gangs.model;

import dev.drawethree.xprison.gangs.api.events.GangJoinEvent;
import dev.drawethree.xprison.gangs.api.events.GangLeaveEvent;
import dev.drawethree.xprison.gangs.enums.GangLeaveReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gang {

	private UUID uuid;
	private UUID gangOwner;
	private List<UUID> gangMembers;
	private List<GangInvitation> pendingInvites;
	private String name;
	private long value;

	public Gang(String name, UUID gangOwner) {
		this.uuid = UUID.randomUUID();
		this.name = name;
		this.gangOwner = gangOwner;
		this.gangMembers = new ArrayList<>();
		this.pendingInvites = new ArrayList<>();
	}

	public boolean containsPlayer(OfflinePlayer p) {
		return this.gangOwner.equals(p.getUniqueId()) || this.gangMembers.contains(p.getUniqueId());
	}

	public boolean isOwner(OfflinePlayer p) {
		return this.gangOwner.equals(p.getUniqueId());
	}

	public boolean leavePlayer(OfflinePlayer p, GangLeaveReason reason) {

		if (!this.gangMembers.contains(p.getUniqueId())) {
			return false;
		}

		if (this.callGangLeaveEvent(p, reason)) {
			return false;
		}

		this.gangMembers.remove(p.getUniqueId());
		return true;
	}

	public GangInvitation invitePlayer(Player invitedBy, Player player) {
		GangInvitation invitation = getGangInvite(player);

		if (invitation != null) {
			return invitation;
		}

		invitation = new GangInvitation(this, player, invitedBy);
		this.pendingInvites.add(invitation);
		return invitation;
	}

	private GangInvitation getGangInvite(OfflinePlayer player) {
		for (GangInvitation gangInvitation : this.pendingInvites) {
			if (gangInvitation.getInvitedPlayer().getUniqueId().equals(player.getUniqueId())) {
				return gangInvitation;
			}
		}
		return null;
	}

	public boolean hasPendingInvite(Player player) {
		return getGangInvite(player) != null;
	}

	private boolean callGangLeaveEvent(OfflinePlayer p, GangLeaveReason reason) {
		GangLeaveEvent event = new GangLeaveEvent(p, this, reason);

		Events.call(event);

		return event.isCancelled();
	}

	public boolean joinPlayer(OfflinePlayer p) {

		if (this.gangMembers.contains(p.getUniqueId())) {
			return false;
		}

		if (this.callGangJoinEvent(p)) {
			return false;
		}

		GangInvitation invitation = getGangInvite(p);
		this.removeInvitation(invitation);

		this.gangMembers.add(p.getUniqueId());
		return true;
	}

	private boolean callGangJoinEvent(OfflinePlayer p) {
		GangJoinEvent event = new GangJoinEvent(p, this);

		Events.call(event);

		return event.isCancelled();
	}


	public List<Player> getOnlinePlayers() {
		return Players.all().stream().filter(this::containsPlayer).collect(Collectors.toList());
	}

	public List<OfflinePlayer> getMembersOffline() {
		List<OfflinePlayer> returnList = new ArrayList<>();
		for (UUID uuid : this.gangMembers) {
			returnList.add(Players.getOfflineNullable(uuid));
		}
		return returnList;
	}

	public OfflinePlayer getOwnerOffline() {
		return Players.getOfflineNullable(this.gangOwner);
	}

	public void disband() {
		this.gangMembers.clear();
		this.gangOwner = null;
	}

	public boolean kickPlayer(OfflinePlayer target) {
		leavePlayer(target, GangLeaveReason.KICK);
		return true;
	}

	public List<GangInvitation> getPendingInvites() {
		return pendingInvites;
	}

	public void removeInvitation(GangInvitation invitation) {
		this.pendingInvites.remove(invitation);
	}

	public boolean canRenameGang(Player player) {
		return isOwner(player);
	}

	public boolean canManageMembers(Player player) {
		return isOwner(player);
	}

	public boolean canDisband(Player player) {
		return isOwner(player);
	}

	public boolean canManageInvites(Player player) {
		return isOwner(player);
	}
}
