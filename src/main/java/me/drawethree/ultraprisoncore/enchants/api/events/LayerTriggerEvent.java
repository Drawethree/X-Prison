package me.drawethree.ultraprisoncore.enchants.api.events;



import lombok.Getter;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.List;

@Getter
public class LayerTriggerEvent extends BlockEnchantEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled;

    public LayerTriggerEvent(Player p, IWrappedRegion mineRegion, List<BlockState> blocksAffected) {
        super(p,mineRegion,blocksAffected);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
