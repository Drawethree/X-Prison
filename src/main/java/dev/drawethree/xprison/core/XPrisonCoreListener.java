package dev.drawethree.xprison.core;

import dev.drawethree.xprison.XPrison;
import me.lucko.helper.Events;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class XPrisonCoreListener {

    private final XPrison plugin;

    public XPrisonCoreListener(XPrison plugin) {
        this.plugin = plugin;
    }

    private void subscribeToPlayerJoinEvent() {
        Events.subscribe(PlayerJoinEvent.class, EventPriority.LOW)
                .handler(e -> {
                    this.plugin.getNicknameService().updatePlayerNickname(e.getPlayer());
                }).bindWith(plugin);
    }
    private void subscribeToPlayerQuitEvent() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
                }).bindWith(plugin);
    }


    public void subscribeToEvents() {
        subscribeToPlayerJoinEvent();
        subscribeToPlayerQuitEvent();
    }
}