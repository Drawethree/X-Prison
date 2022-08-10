package dev.drawethree.ultraprisoncore.autosell.listener;

import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class AutoSellListener {

    private final UltraPrisonAutoSell plugin;

    public AutoSellListener(UltraPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    public void subscribeToEvents() {
        this.subscribeToPlayerJoinEvent();
        this.subscribeToBlockBreakEvent();
        this.subscribeToWorldLoadEvent();
    }

    private void subscribeToWorldLoadEvent() {
        Events.subscribe(WorldLoadEvent.class)
                .handler(e -> this.plugin.getManager().loadPostponedAutoSellRegions(e.getWorld())).bindWith(this.plugin.getCore());
    }

    private void subscribeToPlayerJoinEvent() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> Schedulers.async().runLater(() -> {

                    if (this.plugin.getManager().hasAutoSellEnabled(e.getPlayer())) {
                        PlayerUtils.sendMessage(e.getPlayer(), this.plugin.getAutoSellConfig().getMessage("autosell_enable"));
                        return;
                    }

                    if (this.plugin.getManager().canPlayerEnableAutosellOnJoin(e.getPlayer())) {
                        this.plugin.getManager().toggleAutoSell(e.getPlayer());
                    }
                }, 20)).bindWith(this.plugin.getCore());
    }

    private void subscribeToBlockBreakEvent() {

        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
                .handler(e -> {

                    boolean success;

                    if (!this.plugin.getManager().hasAutoSellEnabled(e.getPlayer())) {
                        success = this.plugin.getManager().givePlayerItem(e.getPlayer(), e.getBlock());
                    } else {
                        success = this.plugin.getManager().autoSellBlock(e.getPlayer(), e.getBlock());
                    }

                    if (success) {
                        e.getBlock().getDrops().clear();
                        e.getBlock().setType(CompMaterial.AIR.toMaterial());
                    } else {
                        e.setCancelled(true);
                    }
                }).bindWith(this.plugin.getCore());
    }
}

