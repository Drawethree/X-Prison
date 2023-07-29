package dev.drawethree.xprison.migrator;

import com.saicone.rtag.RtagItem;
import com.saicone.rtag.tag.TagCompound;
import dev.drawethree.xprison.XPrison;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;

public class ItemMigrator implements Listener {

    private static final String MAIN = "upc";
    private static final String ENCHANT_PATH = "ultra-prison-ench-";
    private static final Map<String, String> PATHS = Map.of(
            "blocks-broken", "blocks",
            "gems-amount", "gems",
            "ultra-prison-pickaxe-level", "level",
            "token-amount", "tokens"
    );

    private final XPrison plugin;

    private boolean enabled;
    private boolean bindJoin;
    private boolean bindInventory;

    public ItemMigrator(XPrison plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        final boolean enabled = plugin.getConfig().getBoolean("item-migrator.enabled", true);
        if (enabled) {
            if (!this.enabled) {
                this.enabled = true;
                Bukkit.getPluginManager().registerEvents(this, plugin);
            }
        } else if (this.enabled) {
            this.enabled = false;
            HandlerList.unregisterAll(this);
            return;
        }
        this.bindJoin = plugin.getConfig().getBoolean("item-migrator.bind.join", true);
        this.bindInventory = plugin.getConfig().getBoolean("item-migrator.bind.inventory", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!bindJoin) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            final Player player = e.getPlayer();
            if (!player.isOnline()) {
                return;
            }
            final Inventory inventory = player.getInventory();
            try {
                for (int i = 0; i < inventory.getContents().length; i++) {
                    final ItemStack item = migrate(inventory.getItem(i));
                    if (item != null) {
                        inventory.setItem(i, item);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 80L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClickInventory(InventoryClickEvent e) {
        if (!bindInventory) {
            return;
        }
        try {
            final ItemStack item = migrate(e.getCurrentItem());
            if (item != null) {
                e.setCancelled(true);
                e.setCurrentItem(item);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public ItemStack migrate(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        final RtagItem tag = new RtagItem(item);
        if (tag.notHasTag()) {
            return null;
        }
        final Map<String, Object> map = TagCompound.getValue(tag.getTag());
        boolean edited = false;
        for (String key : new HashSet<>(map.keySet())) {
            if (PATHS.containsKey(key)) {
                final Object value = map.get(key);
                map.remove(key);
                tag.set(value, MAIN, PATHS.get(key));
                edited = true;
            } else if (key.startsWith(ENCHANT_PATH)) {
                final Object value = map.get(key);
                map.remove(key);
                tag.set(value, MAIN, "enchants", key.substring(18));
                edited = true;
            }
        }
        if (edited) {
            return tag.loadCopy();
        }
        return null;
    }
}
