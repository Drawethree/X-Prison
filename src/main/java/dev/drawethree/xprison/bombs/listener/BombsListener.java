package dev.drawethree.xprison.bombs.listener;

import dev.drawethree.xprison.api.bombs.model.Bomb;
import dev.drawethree.xprison.bombs.XPrisonBombs;
import dev.drawethree.xprison.bombs.model.BombExplodeTask;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public final class BombsListener {

	private final XPrisonBombs plugin;

	public BombsListener(XPrisonBombs plugin) {
		this.plugin = plugin;
	}

	public void register() {
		this.subscribeToPlayerDropItemEvent();
		this.subscribeToPlayerPickupItemEvent();
		this.subscribeToBlockPlaceEvent();
		this.subscribeToPlayerInteractEvent();
	}

	private void subscribeToPlayerInteractEvent() {
		Events.subscribe(PlayerInteractEvent.class).handler(this::handleInteract).bindWith(this.plugin);
	}

	private void subscribeToBlockPlaceEvent() {
		Events.subscribe(BlockPlaceEvent.class, EventPriority.HIGHEST).filter(EventFilters.ignoreCancelled()).handler(this::handleItemPlace).bindWith(this.plugin);
	}

	private void subscribeToPlayerPickupItemEvent() {
		Events.subscribe(PlayerPickupItemEvent.class).handler(this::handleItemPickup).bindWith(this.plugin);
	}

	private void subscribeToPlayerDropItemEvent() {
		Events.subscribe(PlayerDropItemEvent.class).handler(this::handleItemDrop).bindWith(this.plugin);
	}

	private boolean isInCooldown(Player player) {
		return this.plugin.getBombCooldownService().isInCooldown(player);
	}


	private void handleItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		Item item = event.getItemDrop();
		ItemStack itemStack = item.getItemStack();
		this.plugin.getBombsService().getBombFromItem(itemStack).ifPresent(bomb -> {

			if (isInCooldown(player)) {
				PlayerUtils.sendMessage(player, plugin.getConfig().getMessage("cooldown").replace("%time%", String.valueOf(this.plugin.getBombCooldownService().getRemainingCooldown(player))));
				event.setCancelled(true);
				return;
			}

			handleBombDrop(bomb, item, player);
			event.getPlayer().updateInventory();
		});
	}


	private void handleItemPickup(PlayerPickupItemEvent event) {
		if (this.plugin.getBombsService().getBombFromItem(event.getItem().getItemStack()).isPresent()) {
			event.setCancelled(true);
		}
	}

	private void handleItemPlace(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		this.plugin.getBombsService().getBombFromItem(item).ifPresent(bomb -> event.setCancelled(true));
	}

	private void handleInteract(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null || item.getType() == Material.AIR) {
			return;
		}
		this.plugin.getBombsService().getBombFromItem(item).ifPresent(bomb -> event.setCancelled(true));
	}

	private void handleBombDrop(Bomb bomb, Item item, Player player) {
		player.getLocation().getWorld().playSound(player.getLocation(), bomb.getDropSound(), 1.0f, 1.0f);
		BombExplodeTask task = new BombExplodeTask(this.plugin, bomb, player, item);
		task.start();
	}
}
