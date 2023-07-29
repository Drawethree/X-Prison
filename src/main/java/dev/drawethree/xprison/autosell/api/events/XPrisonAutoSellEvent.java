package dev.drawethree.xprison.autosell.api.events;

import dev.drawethree.xprison.api.events.player.XPrisonPlayerEvent;
import dev.drawethree.xprison.autosell.model.AutoSellItemStack;
import dev.drawethree.xprison.autosell.model.SellRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Map;


@Getter
public final class XPrisonAutoSellEvent extends XPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final SellRegion region;
	@Setter
	private Map<AutoSellItemStack, Double> itemsToSell;
	@Setter
	private boolean cancelled;

    /**
     * Called when mined blocks are automatically sold
     *
     * @param player      Player
     * @param reg         IWrappedRegion where block was mined
     * @param itemsToSell ItemStacks to sell with prices
     */
    public XPrisonAutoSellEvent(Player player, SellRegion reg, Map<AutoSellItemStack, Double> itemsToSell) {
		super(player);
		this.player = player;
        this.region = reg;
        this.itemsToSell = itemsToSell;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
