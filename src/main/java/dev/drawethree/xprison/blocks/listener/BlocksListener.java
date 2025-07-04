package dev.drawethree.xprison.blocks.listener;

import dev.drawethree.xprison.blocks.XPrisonBlocks;
import me.lucko.helper.Events;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlocksListener {

	private final XPrisonBlocks plugin;

	public BlocksListener(XPrisonBlocks plugin) {
		this.plugin = plugin;
	}

	public void subscribeToEvents() {
		this.subscribeToPlayerJoinEvent();
		this.subscribeToPlayerQuitEvent();
		this.subscribeToBlockBreakEvent();
	}

	private void subscribeToBlockBreakEvent() {
		Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
				.filter(e -> !e.isCancelled())
				.filter(e -> e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.handler(e -> {
					List<Block> blocks = new ArrayList<>();
					blocks.add(e.getBlock());
					this.plugin.getBlocksManager().handleBlockBreak(e.getPlayer(), blocks, true);
				}).bindWith(plugin);
	}
	private void subscribeToPlayerQuitEvent() {
		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> {
					this.plugin.getBlocksManager().savePlayerData(Collections.singletonList(e.getPlayer()), true, true);
				}).bindWith(plugin);
	}

	private void subscribeToPlayerJoinEvent() {
		Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> {
					this.plugin.getBlocksManager().loadPlayerData(Collections.singleton(e.getPlayer()));
				}).bindWith(plugin);
	}
}
