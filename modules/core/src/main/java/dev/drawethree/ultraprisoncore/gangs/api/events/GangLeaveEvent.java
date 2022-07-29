package dev.drawethree.ultraprisoncore.gangs.api.events;

import dev.drawethree.ultraprisoncore.gangs.enums.GangLeaveReason;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GangLeaveEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private boolean cancelled;

	@Getter
	private final OfflinePlayer player;

	@Getter
	private final Gang gang;

	@Getter
	private final GangLeaveReason leaveReason;

	/**
	 * Called when player leaves a gang
	 *
	 * @param player      Player
	 * @param gang        Gang
	 * @param leaveReason GangLeaveReason
	 */
	public GangLeaveEvent(OfflinePlayer player, Gang gang, GangLeaveReason leaveReason) {
		this.player = player;
		this.gang = gang;
		this.leaveReason = leaveReason;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
}
