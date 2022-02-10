package me.drawethree.ultraprisoncore.gangs.model;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.api.events.GangJoinEvent;
import me.drawethree.ultraprisoncore.gangs.api.events.GangLeaveEvent;
import me.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class Gang {

	@Getter
	private UUID uuid;

	@Getter
	private UUID gangOwner;
	private List<UUID> gangMembers;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private int value;


	public Gang(String name, UUID gangOwner) {
		this.uuid = UUID.randomUUID();
		this.name = name;
		this.gangOwner = gangOwner;
		this.gangMembers = new ArrayList<>();
	}

	public Gang(UUID uuid, String gangName, UUID owner, List<UUID> members, int value) {
		this.uuid = uuid;
		this.name = gangName;
		this.gangOwner = owner;
		this.gangMembers = members;
		this.value = value;
	}

	public boolean containsPlayer(OfflinePlayer p) {
		return this.gangOwner.equals(p.getUniqueId()) || this.gangMembers.contains(p.getUniqueId());
	}

	public boolean isOwner(OfflinePlayer p) {
		return this.gangOwner.equals(p.getUniqueId());
	}

	public boolean leavePlayer(Player p) {

		if (!this.gangMembers.contains(p.getUniqueId())) {
			return false;
		}

		GangLeaveEvent event = new GangLeaveEvent(p, this);

		Events.call(event);

		if (event.isCancelled()) {
			return false;
		}

		this.gangMembers.remove(p.getUniqueId());
		this.getOnlinePlayers().forEach(player -> PlayerUtils.sendMessage(player, UltraPrisonGangs.getInstance().getMessage("gang-player-left").replace("%player%", p.getName())));
		PlayerUtils.sendMessage(p, UltraPrisonGangs.getInstance().getMessage("gang-left").replace("%gang%", this.name));
		return true;
	}

	public boolean joinPlayer(Player p) {

		if (this.gangMembers.contains(p.getUniqueId())) {
			return false;
		}

		GangJoinEvent event = new GangJoinEvent(p, this);

		Events.call(event);

		if (event.isCancelled()) {
			return false;
		}

		this.getOnlinePlayers().forEach(player -> PlayerUtils.sendMessage(player, UltraPrisonGangs.getInstance().getMessage("gang-player-joined").replace("%player%", p.getName())));
		this.gangMembers.add(p.getUniqueId());
		PlayerUtils.sendMessage(p, UltraPrisonGangs.getInstance().getMessage("gang-joined").replace("%gang%", this.name));
		return true;
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

		if (!this.gangMembers.contains(target.getUniqueId())) {
			return false;
		}

		this.gangMembers.remove(target.getUniqueId());
		this.getOnlinePlayers().forEach(player -> PlayerUtils.sendMessage(player, UltraPrisonGangs.getInstance().getMessage("gang-player-kicked").replace("%player%", target.getName())));
		if (target.isOnline()) {
			PlayerUtils.sendMessage(target.getPlayer(), UltraPrisonGangs.getInstance().getMessage("gang-kicked").replace("%gang%", this.name));
		}
		return true;
	}
}
