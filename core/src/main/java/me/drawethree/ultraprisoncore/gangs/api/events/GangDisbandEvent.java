package me.drawethree.ultraprisoncore.gangs.api.events;

import lombok.Getter;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GangDisbandEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private boolean cancelled;

    @Getter
    private Gang gang;

    public GangDisbandEvent(Gang gang) {
        this.gang = gang;
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

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
