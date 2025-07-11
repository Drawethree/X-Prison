package dev.drawethree.xprison.enchants.managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RespawnManager {

	private final Map<UUID, List<ItemStack>> respawnItems;


	public RespawnManager() {
		this.respawnItems = new HashMap<>();
	}

	public void addRespawnItems(Player player, List<ItemStack> items) {
		this.respawnItems.put(player.getUniqueId(), items);
	}

	public void handleRespawn(Player player) {
		if (this.respawnItems.containsKey(player.getUniqueId())) {
			this.respawnItems.remove(player.getUniqueId()).forEach(itemStack -> {
				player.getInventory().addItem(itemStack);
			});
		}
	}
}
