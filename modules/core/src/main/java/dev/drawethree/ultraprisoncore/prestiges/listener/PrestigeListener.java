package dev.drawethree.ultraprisoncore.prestiges.listener;

import dev.drawethree.ultraprisoncore.prestiges.UltraPrisonPrestiges;
import me.lucko.helper.Events;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PrestigeListener {
    private final UltraPrisonPrestiges plugin;

    public PrestigeListener(UltraPrisonPrestiges plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.subscribePlayerJoinEvent();
        this.subscribePlayerQuitEvent();
    }

    private void subscribePlayerQuitEvent() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.plugin.getPrestigeManager().savePlayerData(e.getPlayer(), true, true)).bindWith(plugin.getCore());
    }

    private void subscribePlayerJoinEvent() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.plugin.getPrestigeManager().loadPlayerPrestige(e.getPlayer())).bindWith(plugin.getCore());
    }


}
