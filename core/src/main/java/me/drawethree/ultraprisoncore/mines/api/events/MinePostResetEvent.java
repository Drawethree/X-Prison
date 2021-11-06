package me.drawethree.ultraprisoncore.mines.api.events;

import lombok.Getter;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MinePostResetEvent extends Event {

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

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
