package dev.drawethree.xprison.bombs.model;

import dev.drawethree.xprison.api.bombs.events.BombExplodeEvent;
import dev.drawethree.xprison.api.bombs.model.Bomb;
import dev.drawethree.xprison.bombs.XPrisonBombs;
import dev.drawethree.xprison.bombs.handler.BlockHandler;
import dev.drawethree.xprison.bombs.handler.SellHandler;
import me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public final class BombExplodeTask extends BukkitRunnable {

	private final XPrisonBombs plugin;
	private final Bomb bomb;
	private final Player player;
	private final Item item;

	private BukkitTask task;

	public BombExplodeTask(XPrisonBombs plugin, Bomb bomb, Player player, Item item) {
		this.plugin = plugin;
		this.bomb = bomb;
		this.player = player;
		this.item = item;
	}

	@Override
	public void run() {

		List<Block> affectedBlocks = this.getBlocks(item.getLocation(), bomb.getRadius());
		BombExplodeEvent event = this.callBombExplodeEvent(affectedBlocks);

		if (event.isCancelled()) {
			this.player.getInventory().addItem(item.getItemStack());
			item.remove();
			return;
		}

		item.getLocation().getWorld().playSound(item.getLocation(), bomb.getExplodeSound(), 1.0f, 1.0f);

		affectedBlocks = getBlocksHandler().handle(event.getAffectedBlocks());
		this.handleSelling(player, affectedBlocks);
		affectedBlocks.forEach(block -> block.setType(Material.AIR));
		item.remove();
	}

	private void handleSelling(Player player, List<Block> affectedBlocks) {
		if (getSellHandler() == null) {
			return;
		}
		getSellHandler().sell(player, affectedBlocks);
	}

	private SellHandler getSellHandler() {
		return this.plugin.getBombsService().getSellHandler();
	}

	private BlockHandler getBlocksHandler() {
		return this.plugin.getBombsService().getBlockHandler();
	}

	public void start() {
		stop();
		this.task = this.runTaskLater(plugin.getCore(), bomb.getExplosionDelay() * 20L);
	}

	public void stop() {
		if (this.task != null) {
			this.task.cancel();
		}
	}


	private BombExplodeEvent callBombExplodeEvent(List<Block> affectedBlocks) {
		BombExplodeEvent event = new BombExplodeEvent(bomb, player, item.getLocation(), affectedBlocks);
		Events.call(event);
		return event;
	}

	public List<Block> getBlocks(final Location center, final int radius) {
		List<Block> sphere = new ArrayList<>();
		for (int Y = -radius; Y < radius; Y++)
			for (int X = -radius; X < radius; X++)
				for (int Z = -radius; Z < radius; Z++)
					if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
						final Block block = center.getWorld().getBlockAt(X + center.getBlockX(), Y + center.getBlockY(), Z + center.getBlockZ());
						sphere.add(block);
					}
		return sphere;
	}
}