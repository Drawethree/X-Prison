package dev.drawethree.ultraprisoncore.utils.inventory;

import org.bukkit.inventory.Inventory;

public final class InventoryUtils {

    public static boolean hasSpace(Inventory inventory) {
        return inventory.firstEmpty() != -1;
    }
}
