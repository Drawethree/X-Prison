package dev.drawethree.xprison.autosell.listener;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.SellRegion;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class AutoSellListener {

    private final XPrisonAutoSell plugin;

    public AutoSellListener(XPrisonAutoSell plugin) {
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
				.handler(e -> Schedulers.sync().runLater(() -> {

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
                .filter(e -> !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
                .handler(e -> {

                    SellRegion sellRegion = this.plugin.getManager().getAutoSellRegion(e.getBlock().getLocation());

                    if (sellRegion == null) {
                        return;
                    }

                    boolean success = false;

                    if (this.plugin.getManager().hasAutoSellEnabled(e.getPlayer())) {
                        success = this.plugin.getManager().autoSellBlock(e.getPlayer(), e.getBlock());
                    }

                    if (!success) {
                        success = this.plugin.getManager().givePlayerItem(e.getPlayer(), e.getBlock());
                    }

                    if (success) {
                        // Do not set block to air due compatibility issues
                        e.setDropItems(false);
                    } else {
                        e.setCancelled(true);
                    }
                }).bindWith(this.plugin.getCore());
    }
}

