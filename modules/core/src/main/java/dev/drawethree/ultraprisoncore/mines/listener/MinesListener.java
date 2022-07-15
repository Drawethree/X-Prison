package dev.drawethree.ultraprisoncore.mines.listener;

import dev.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import dev.drawethree.ultraprisoncore.mines.managers.MineManager;
import dev.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.lucko.helper.Events;
import me.lucko.helper.serialize.Position;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class MinesListener {

	private final UltraPrisonMines plugin;

	public MinesListener(UltraPrisonMines plugin) {
		this.plugin = plugin;
	}

	public void register() {
		this.subscribeToBlockBreakEvent();
		this.subscribeToPlayerInteractEvent();
	}

	private void subscribeToBlockBreakEvent() {
		Events.subscribe(BlockBreakEvent.class, EventPriority.MONITOR)
				.filter(e -> !e.isCancelled())
				.handler(e -> {
					Mine mine = this.plugin.getManager().getMineAtLocation(e.getBlock().getLocation());

					if (mine == null) {
						return;
					}

					if (mine.isResetting()) {
						e.setCancelled(true);
						return;
					}

					mine.handleBlockBreak(Arrays.asList(e.getBlock()));
				}).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerInteractEvent() {
		Events.subscribe(PlayerInteractEvent.class)
				.filter(e -> e.getItem() != null && e.getItem().isSimilar(MineManager.SELECTION_TOOL) && e.getClickedBlock() != null)
				.handler(e -> {
					int pos = e.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : e.getAction() == Action.RIGHT_CLICK_BLOCK ? 2 : -1;

					if (pos == -1) {
						return;
					}

					this.plugin.getManager().selectPosition(e.getPlayer(), pos, Position.of(e.getClickedBlock()));
				}).bindWith(this.plugin.getCore());
	}
}
