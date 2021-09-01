package me.drawethree.ultraprisoncore.gangs.api.events;

import lombok.Getter;
import me.drawethree.ultraprisoncore.gangs.model.Gang;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GangLeaveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private boolean cancelled;

    @Getter
    private Player player;

    @Getter
    private Gang gang;

    public GangLeaveEvent(Player player, Gang gang) {
        this.player = player;
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
