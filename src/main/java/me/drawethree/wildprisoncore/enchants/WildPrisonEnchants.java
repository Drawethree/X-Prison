package me.drawethree.wildprisoncore.enchants;

import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.enchants.api.WildPrisonEnchantsAPI;
import me.drawethree.wildprisoncore.enchants.api.WildPrisonEnchantsAPIImpl;
import me.drawethree.wildprisoncore.enchants.gui.DisenchantGUI;
import me.drawethree.wildprisoncore.enchants.gui.EnchantGUI;
import me.drawethree.wildprisoncore.enchants.managers.EnchantsManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class WildPrisonEnchants {

    @Getter
    private static WildPrisonEnchants instance;

    @Getter
    private WildPrisonEnchantsAPI api;

    @Getter
    private EnchantsManager enchantsManager;

    @Getter
    private FileManager.Config config;

    @Getter
    private WildPrisonCore core;

    private HashMap<String, String> messages;
    private List<UUID> disabledJackHammer = new ArrayList<>();
    private List<UUID> disabledExplosive = new ArrayList<>();

    public WildPrisonEnchants(WildPrisonCore wildPrisonCore) {
        instance = this;
        this.config = wildPrisonCore.getFileManager().getConfig("enchants.yml").saveDefaultConfig();
        this.core = wildPrisonCore;
        this.enchantsManager = new EnchantsManager(this);
        this.api = new WildPrisonEnchantsAPIImpl(enchantsManager);
        this.loadMessages();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    public void enable() {
        this.registerCommands();
        this.registerEvents();
    }

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
                }).registerAndBind(core, "disenchant", "dise");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        toggleExplosive(c.sender());
                    }
                }).registerAndBind(core, "explosive");
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        toggleJackHammer(c.sender());
                    }
                }).registerAndBind(core, "jackhammer");
    }

    private void toggleJackHammer(Player sender) {
        if (disabledJackHammer.contains(sender.getUniqueId())) {
            sender.sendMessage(getMessage("jackhammer_enabled"));
            disabledJackHammer.remove(sender.getUniqueId());
        } else {
            sender.sendMessage(getMessage("jackhammer_disabled"));
            disabledJackHammer.add(sender.getUniqueId());
        }
    }

    private void toggleExplosive(Player sender) {
        if (disabledExplosive.contains(sender.getUniqueId())) {
            sender.sendMessage(getMessage("explosive_enabled"));
            disabledExplosive.remove(sender.getUniqueId());
        } else {
            sender.sendMessage(getMessage("explosive_disabled"));
            disabledExplosive.add(sender.getUniqueId());
        }
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getItem() != null && e.getItem().getType() == Material.DIAMOND_PICKAXE && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
                .handler(e -> {
                    new EnchantGUI(e.getPlayer(), e.getItem()).open();
                }).bindWith(core);
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
                }).bindWith(core);
        Events.subscribe(BlockBreakEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE)
                .handler(e -> {
                    enchantsManager.addBlocksBrokenToItem(e.getPlayer(), 1);
                    enchantsManager.handleBlockBreak(e, e.getPlayer().getItemInHand());
                }).bindWith(core);

    }

    public String getMessage(String key) {
        return messages.get(key.toLowerCase());
    }

    public boolean hasJackHammerDisabled(Player p) {
        return disabledJackHammer.contains(p.getUniqueId());
    }

    public boolean hasExplosiveDisabled(Player p) {
        return disabledExplosive.contains(p.getUniqueId());
    }

}
