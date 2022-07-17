package dev.drawethree.ultraprisoncore.autosell.api.events;

import dev.drawethree.ultraprisoncore.autosell.model.AutoSellItemStack;
import dev.drawethree.ultraprisoncore.autosell.model.SellRegion;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;


@Getter
public class UltraPrisonAutoSellEvent extends Event implements Cancellable {

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
    public UltraPrisonAutoSellEvent(Player player, SellRegion reg, Map<AutoSellItemStack, Double> itemsToSell) {
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
