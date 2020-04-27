package me.drawethree.wildprisoncore.enchants.api.events;


import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;

@Getter
public class ExplosionTriggerEvent extends BlockEnchantEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public ExplosionTriggerEvent(Player p, ProtectedRegion mineRegion, List<BlockState> blocksAffected) {
        super(p,mineRegion,blocksAffected);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
