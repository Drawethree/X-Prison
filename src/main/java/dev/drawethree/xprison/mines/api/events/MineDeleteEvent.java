package dev.drawethree.xprison.mines.api.events;

import dev.drawethree.xprison.api.events.XPrisonEvent;
import dev.drawethree.xprison.mines.model.mine.Mine;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MineDeleteEvent extends XPrisonEvent implements Cancellable {

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Getter
	@Setter
	private boolean cancelled;

	@Getter
	private Mine mine;

	/**
	 * Called when mine is deleted
	 *
	 * @param mine MIne
	 */
	public MineDeleteEvent(Mine mine) {
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
