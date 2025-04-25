package dev.drawethree.xprison.utils;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.tokens.XPrisonTokens;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PersistentActionBar extends BukkitRunnable {

    private final XPrison plugin;
    private static final Map<UUID, PersistentActionBar> tasks = new ConcurrentHashMap<>();

    private final Player player;
    private final Supplier<String> messageSupplier;

    public PersistentActionBar(XPrison plugin, Player player, Supplier<String> messageSupplier) {
        this.plugin = plugin;
        this.player = player;
        this.messageSupplier = messageSupplier;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            stop(player);
            return;
        }

        String message = messageSupplier.get();
        player.sendActionBar(Component.text(message
                .replace("%RankupProgress%",  String.valueOf(plugin.getRanks().getRanksManager().getRankupProgress(player)))
                .replace("%RankupProgressBar%",  plugin.getRanks().getRanksManager().getRankupProgressBar(player))));
    }

    public static void start(Player player, Supplier<String> messageSupplier, XPrison plugin) {
        stop(player); // Evita duplicados

        PersistentActionBar task = new PersistentActionBar(plugin, player, messageSupplier);
        tasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 20L); // Cada segundo (20 ticks)
    }

    public static void stop(@NotNull Player player) {
        PersistentActionBar existing = tasks.remove(player.getUniqueId());
        if (existing != null) {
            existing.cancel();
        }
    }
}

