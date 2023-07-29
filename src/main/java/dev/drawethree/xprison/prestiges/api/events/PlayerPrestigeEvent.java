package dev.drawethree.xprison.prestiges.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import dev.drawethree.xprison.prestiges.model.Prestige;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public final class PlayerPrestigeEvent extends XPrisonPlayerEvent implements Cancellable {


	private static final HandlerList handlers = new HandlerList();

	@Getter
	private final Prestige oldPrestige;

	@Getter
	@Setter
	private Prestige newPrestige;

	@Getter
	@Setter
	private boolean cancelled;

	/**
	 * Called when player receive gems
	 *
	 * @param player      Player
	 * @param oldPrestige old prestige
	 * @param newPrestige new prestige
	 */
	public PlayerPrestigeEvent(Player player, Prestige oldPrestige, Prestige newPrestige) {
		super(player);
		this.oldPrestige = oldPrestige;
		this.newPrestige = newPrestige;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
