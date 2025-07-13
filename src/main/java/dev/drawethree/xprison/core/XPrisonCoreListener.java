package dev.drawethree.xprison.core;

import dev.drawethree.xprison.XPrisonLite;
import me.lucko.helper.Events;
import org.bukkit.event.player.PlayerQuitEvent;

public class XPrisonCoreListener {

    private final XPrisonLite plugin;

    public XPrisonCoreListener(XPrisonLite plugin) {
        this.plugin = plugin;
    }

    private void subscribeToPlayerQuitEvent() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
                }).bindWith(plugin);
    }


    public void subscribeToEvents() {
        subscribeToPlayerQuitEvent();
    }
}