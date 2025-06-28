package dev.drawethree.xprison.gangs.model;

import dev.drawethree.xprison.api.gangs.enums.GangLeaveReason;
import dev.drawethree.xprison.api.gangs.events.GangJoinEvent;
import dev.drawethree.xprison.api.gangs.events.GangLeaveEvent;
import dev.drawethree.xprison.api.gangs.model.Gang;
import dev.drawethree.xprison.api.gangs.model.GangInvitation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GangImpl implements Gang {

	private UUID uuid;
	private UUID gangOwner;
	private List<UUID> gangMembers;
	private List<GangInvitationImpl> pendingInvites;
	private String name;
	private long value;

	public GangImpl(String name, UUID gangOwner) {
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

	@Override
	public boolean isInGang(OfflinePlayer offlinePlayer) {
		return getMembersOffline().contains(offlinePlayer);
	}

	public GangInvitationImpl invitePlayer(Player invitedBy, OfflinePlayer player) {
		GangInvitationImpl invitation = getGangInvite(player);

		if (invitation != null) {
			return invitation;
		}

		invitation = new GangInvitationImpl(this, player, invitedBy);
		this.pendingInvites.add(invitation);
		return invitation;
	}

	private GangInvitationImpl getGangInvite(OfflinePlayer player) {
		for (GangInvitationImpl gangInvitationImpl : this.pendingInvites) {
			if (gangInvitationImpl.getInvitedPlayer().getUniqueId().equals(player.getUniqueId())) {
				return gangInvitationImpl;
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

		GangInvitationImpl invitation = getGangInvite(p);
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

	@Override
	public GangInvitation invitePlayer(OfflinePlayer offlinePlayer) {
		return invitePlayer(null,offlinePlayer);
	}

	@Override
	public void removeInvite(GangInvitation gangInvitation) {
		GangInvitationImpl impl = getGangInvite(gangInvitation.getInvitedPlayer());
		removeInvitation(impl);
	}

	public boolean kickPlayer(OfflinePlayer target) {
		leavePlayer(target, GangLeaveReason.KICK);
		return true;
	}

	public List<GangInvitationImpl> getPendingInvitations() {
		return pendingInvites;
	}

	public List<GangInvitation> getPendingInvites() {
		return Collections.unmodifiableList(pendingInvites);
	}

	@Override
	public long getGangValue() {
		return getValue();
	}

	@Override
	public Collection<Player> getOnlineMembers() {
		return getOnlinePlayers();
	}

	@Override
	public Collection<OfflinePlayer> getAllMembers() {
		return getMembersOffline();
	}

	public void removeInvitation(GangInvitationImpl invitation) {
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
