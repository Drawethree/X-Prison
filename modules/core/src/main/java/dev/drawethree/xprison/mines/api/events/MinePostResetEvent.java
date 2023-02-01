package dev.drawethree.xprison.mines.api.events;

import dev.drawethree.xprison.api.events.XPrisonEvent;
import dev.drawethree.xprison.mines.model.mine.Mine;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MinePostResetEvent extends XPrisonEvent {

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Getter
	private Mine mine;

	/**
	 * Fired when mine reset was completed
	 *
	 * @param mine Mine
	 */
	public MinePostResetEvent(Mine mine) {
		this.mine = mine;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
}
