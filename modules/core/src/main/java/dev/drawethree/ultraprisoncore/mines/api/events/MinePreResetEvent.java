package dev.drawethree.ultraprisoncore.mines.api.events;

import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MinePreResetEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Getter
	private Mine mine;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Fired when mine is going to be reset
	 *
	 * @param mine Mine
	 */
	public MinePreResetEvent(Mine mine) {
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
