package dev.drawethree.xprison.pickaxelevels.listener;

import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PickaxeProtectionListener {

    private final XPrisonPickaxeLevels plugin;
    private final Map<UUID, List<ItemStack>> savedPickaxes = new HashMap<>();

    public PickaxeProtectionListener(XPrisonPickaxeLevels plugin) {
        this.plugin = plugin;
        registerEvents();
    }

    private void registerEvents() {

        // Drop
        Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDrop(PlayerDropItemEvent e) {
                ItemStack item = e.getItemDrop().getItemStack();
                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    if (isTheirPickaxe(item, e.getPlayer())) {
                        e.setCancelled(true);
                    }
                }
            }
        }, plugin.getCore());

        // Pickup
        plugin.getCore().getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPickup(EntityPickupItemEvent e) {
                if (!(e.getEntity() instanceof Player)) return;
                Player player = (Player) e.getEntity();
                ItemStack item = e.getItem().getItemStack();
                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    if (!isTheirPickaxe(item, player)) {
                        e.setCancelled(true);
                    }
                }
            }
        }, plugin.getCore());

        // Inventory click
        plugin.getCore().getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (e.getClickedInventory() == null) return;
                ItemStack item = e.getCurrentItem();
                if (item == null) return;
                if (!(e.getWhoClicked() instanceof Player)) return;
                Player player = (Player) e.getWhoClicked();

                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    if (!isTheirPickaxe(item, player)) {
                        e.setCancelled(true);
                    }
                }
            }
        }, plugin.getCore());

        // Inventory drag
        plugin.getCore().getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDrag(InventoryDragEvent e) {
                ItemStack item = e.getOldCursor();
                if (!(e.getWhoClicked() instanceof Player)) return;
                Player player = (Player) e.getWhoClicked();

                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    if (!isTheirPickaxe(item, player)) {
                        e.setCancelled(true);
                    }
                }
            }
        }, plugin.getCore());

        // Hoppers
        plugin.getCore().getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onHopper(InventoryMoveItemEvent e) {
                ItemStack item = e.getItem();
                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    if (plugin.getCore().isPickaxeSupported(item)) {
                        e.setCancelled(true);
                    }
                }
            }
        }, plugin.getCore());

        // ItemFrame
        plugin.getCore().getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteractEntity(PlayerInteractEntityEvent e) {
                if (!(e.getRightClicked() instanceof ItemFrame)) return;

                ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    if (!isTheirPickaxe(item, e.getPlayer())) {
                        e.setCancelled(true);
                    }
                }
            }
        }, plugin.getCore());

        // Anvil
        plugin.getCore().getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onAnvil(PrepareAnvilEvent e) {
                for (ItemStack item : e.getInventory().getContents()) {
                    if (item == null || item.getItemMeta() == null) continue;
                    if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                        Player player = (Player) e.getView().getPlayer();
                        if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                            if (!isTheirPickaxe(item, player)) {
                                e.setResult(null);
                            }
                        }
                    }
                }
            }
        }, plugin.getCore());
    }

    /**
     * Check if the item is a valid pickaxe and if the player is the owner of the pickaxe
     * @param item The item to check
     * @param player The player to check
     * @return True if the item is a valid pickaxe and the player is the owner of the pickaxe
     */
    private boolean isTheirPickaxe(ItemStack item, Player player) {
        plugin.getCore().debug("Checking if item is their pickaxe", plugin);
        if (item == null || item.getItemMeta() == null) {
            plugin.getCore().debug("Item is null or has no meta", plugin);
            return false;
        }

        if (!isPrisonPickaxe(item)) {
            plugin.getCore().debug("Item is not a prison pickaxe", plugin);
            return true;
        }

        if (!hasPlayerName(item, player)){
            plugin.getCore().debug("Item does not have player name", plugin);
            return false;
        }

        return hasPlayerName(item, player) && isPrisonPickaxe(item);
    }
    
    private boolean hasPlayerName(@NotNull ItemStack item, @NotNull Player player) {

        NamespacedKey key = new NamespacedKey(plugin.getCore(), "prison-player");
        if (!item.getPersistentDataContainer().has(key, PersistentDataType.STRING)){
            plugin.getCore().debug("Item does not have player name", plugin);
            return false;
        }
        
        String uuid = item.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (uuid == null){
            plugin.getCore().debug("UUID is null", plugin);
            return false;
        }

        plugin.getCore().debug("UUID is " + uuid, plugin);
        return uuid.equals(player.getUniqueId().toString());
    }

    private boolean isPrisonPickaxe(@NotNull ItemStack item) {
        return item.getPersistentDataContainer().has(new NamespacedKey(plugin.getCore(), "prison-pickaxe"));
    }
}
