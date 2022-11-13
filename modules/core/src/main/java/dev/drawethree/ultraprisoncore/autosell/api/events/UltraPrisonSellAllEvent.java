package dev.drawethree.ultraprisoncore.autosell.api.events;

import dev.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerEvent;
import dev.drawethree.ultraprisoncore.autosell.model.AutoSellItemStack;
import dev.drawethree.ultraprisoncore.autosell.model.SellRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Map;


@Getter
public final class UltraPrisonSellAllEvent extends UltraPrisonPlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final SellRegion region;

	@Getter
	@Setter
	private Map<AutoSellItemStack, Double> itemsToSell;

    @Getter
    @Setter
    private boolean cancelled;

	/**
	 * Called when mined blocks are automatically sold
	 *
	 * @param player      Player
	 * @param reg         SellRegion where block was mined
	 * @param itemsToSell Map of items to sell with prices as values
	 */
	public UltraPrisonSellAllEvent(Player player, SellRegion reg, Map<AutoSellItemStack, Double> itemsToSell) {
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
