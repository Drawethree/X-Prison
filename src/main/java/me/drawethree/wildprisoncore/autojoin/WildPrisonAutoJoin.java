package me.drawethree.wildprisoncore.autojoin;

import me.drawethree.wildprisoncore.WildPrisonCore;
import me.lucko.helper.Events;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public final class WildPrisonAutoJoin {


    private final WildPrisonCore core;

    public WildPrisonAutoJoin(WildPrisonCore wildPrisonCore) {
        this.core = wildPrisonCore;
    }


    public void enable() {
        Events.subscribe(PlayerJoinEvent.class, EventPriority.HIGHEST)
                .handler(e -> {
                    e.getPlayer().performCommand("spawn");
                }).bindWith(this.core);
    }

    public void disable() {

    }
}
