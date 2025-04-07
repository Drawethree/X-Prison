package dev.drawethree.xprison.pickaxelevels.listener;

import dev.drawethree.xprison.pickaxelevels.XPrisonPickaxeLevels;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

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
                if (isProtectedPickaxe(item, e.getPlayer())) {
                    e.setCancelled(true);
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
                if (isProtectedPickaxe(item, player)) return; // Ok
                if (plugin.getPickaxeLevelsManager().getPickaxeLevel(item).isPresent()) {
                    e.setCancelled(true);
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
                    if (!isProtectedPickaxe(item, player)) {
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
                    if (!isProtectedPickaxe(item, player)) {
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
                    e.setCancelled(true);
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
                    if (!isProtectedPickaxe(item, e.getPlayer())) {
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
                        if (!isProtectedPickaxe(item, player)) {
                            e.setResult(null);
                        }
                    }
                }
            }
        }, plugin.getCore());
    }

    private boolean isProtectedPickaxe(ItemStack item, Player player) {
        if (item == null || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) return false;
        String displayName = LegacyComponentSerializer.legacySection().serialize(Objects.requireNonNull(item.getItemMeta().displayName()));
        return displayName.contains(player.getName());
    }
}
