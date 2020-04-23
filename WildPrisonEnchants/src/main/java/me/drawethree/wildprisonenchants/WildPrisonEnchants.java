package me.drawethree.wildprisonenchants;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import me.drawethree.wildprisonenchants.api.WildPrisonEnchantsAPI;
import me.drawethree.wildprisonenchants.api.WildPrisonEnchantsAPIImpl;
import me.drawethree.wildprisonenchants.gui.DisenchantGUI;
import me.drawethree.wildprisonenchants.gui.EnchantGUI;
import me.drawethree.wildprisonenchants.managers.EnchantsManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;

public final class WildPrisonEnchants extends ExtendedJavaPlugin {

    @Getter
    private static WildPrisonEnchants instance;

    @Getter
    private static WildPrisonEnchantsAPI api;

    @Getter
    private static EnchantsManager enchantsManager;

    @Getter
    private static Economy economy;

    private static HashMap<String, String> messages;

    @Override
    public void load() {
        instance = this;
        enchantsManager = new EnchantsManager(this);
        api = new WildPrisonEnchantsAPIImpl(enchantsManager);

        saveDefaultConfig();
        loadMessages();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, Text.colorize(getConfig().getString("messages." + key)));
        }
    }

    @Override
    public void enable() {
        this.registerCommands();
        this.registerEvents();
        this.setupEconomy();
    }

    @Override
    public void disable() {
        // Plugin shutdown logic
    }

    private void registerCommands() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    ItemStack pickAxe = c.sender().getItemInHand();
                    if (pickAxe == null || pickAxe.getType() != Material.DIAMOND_PICKAXE) {
                        c.sender().sendMessage(getMessage("no_pickaxe_found"));
                        return;
                    }
                    new DisenchantGUI(c.sender(), pickAxe).open();
                }).registerAndBind(this, "disenchant", "dise");
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getItem() != null && e.getItem().getType() == Material.DIAMOND_PICKAXE && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
                .handler(e -> {
                    new EnchantGUI(e.getPlayer(), e.getItem()).open();
                }).bindWith(this);
        Events.subscribe(PlayerItemHeldEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL)
                .handler(e -> {
                    Schedulers.sync().runLater(() -> {
                        ItemStack oldItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
                        ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
                        if (newItem != null && newItem.getType() == Material.DIAMOND_PICKAXE) {
                            enchantsManager.handlePickaxeEquip(e.getPlayer(), newItem);
                            if (!newItem.hasItemMeta() && !newItem.getItemMeta().hasLore()) {
                                enchantsManager.applyLoreToPickaxe(newItem);
                            }
                        } else if (oldItem != null && oldItem.getType() == Material.DIAMOND_PICKAXE) {
                            enchantsManager.handlePickaxeUnequip(e.getPlayer(), oldItem);
                        }
                    }, 1);
                }).bindWith(this);
        Events.subscribe(BlockBreakEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE)
                .handler(e -> {
                    enchantsManager.addBlocksBrokenToItem(e.getPlayer(), 1);
                    enchantsManager.handleBlockBreak(e, e.getPlayer().getItemInHand());
                }).bindWith(this);

    }

    public static String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }
}
