package dev.drawethree.ultraprisoncore.gangs.api.events;

import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class GangJoinEvent extends UltraPrisonPlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private boolean cancelled;

	@Getter
	private final OfflinePlayer player;

	@Getter
	private final Gang gang;

	/**
	 * Called when player joins a gang
	 *
	 * @param player Player
	 * @param gang   Gang
	 */
	public GangJoinEvent(OfflinePlayer player, Gang gang) {
		super(player);
		this.player = player;
		this.gang = gang;
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
