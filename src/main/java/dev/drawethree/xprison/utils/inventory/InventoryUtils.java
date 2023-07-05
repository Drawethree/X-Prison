package dev.drawethree.xprison.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtils {

	private InventoryUtils() {
		throw new UnsupportedOperationException("Cannot instantiate.");
	}

	public static boolean hasSpace(Inventory inventory) {
		return inventory.firstEmpty() != -1;
	}

	public static int getInventorySlot(Player player, ItemStack item) {
		if (item == null) {
			return -1;
		}
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item1 = player.getInventory().getItem(i);

			if (item1 == null) {
				continue;
			}

			if (item1.equals(item)) {
				return i;
			}
		}
		return -1;
	}
}
