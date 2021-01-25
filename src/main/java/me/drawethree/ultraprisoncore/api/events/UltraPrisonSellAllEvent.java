package me.drawethree.ultraprisoncore.api.events;

import lombok.Getter;
import lombok.Setter;
import me.drawethree.ultraprisoncore.autosell.AutoSellRegion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.codemc.worldguardwrapper.region.IWrappedRegion;


@Getter
public class UltraPrisonSellAllEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final AutoSellRegion region;

    /**
     * Called when mined blocks are automatically sold
     *
     * @param player         Player
     * @param reg            AutoSellRegion where block was mined
     */
    public UltraPrisonSellAllEvent(Player player, AutoSellRegion reg) {
        this.player = player;
        this.region = reg;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
