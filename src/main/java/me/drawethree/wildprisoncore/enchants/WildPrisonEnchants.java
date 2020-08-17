package me.drawethree.wildprisoncore.enchants;

import lombok.Getter;
import lombok.NonNull;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.enchants.api.WildPrisonEnchantsAPI;
import me.drawethree.wildprisoncore.enchants.api.WildPrisonEnchantsAPIImpl;
import me.drawethree.wildprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import me.drawethree.wildprisoncore.enchants.gui.DisenchantGUI;
import me.drawethree.wildprisoncore.enchants.gui.EnchantGUI;
import me.drawethree.wildprisoncore.enchants.managers.EnchantsManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class WildPrisonEnchants {

    @Getter
    private static WildPrisonEnchants instance;
    private final HashMap<UUID, ItemStack> currentPickaxes = new HashMap<>();

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
    private CooldownMap<Player> valueCooldown = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));

    public WildPrisonEnchants(WildPrisonCore wildPrisonCore) {
        instance = this;
        this.config = wildPrisonCore.getFileManager().getConfig("enchants.yml").copyDefaults(true).save();
        this.core = wildPrisonCore;
        this.enchantsManager = new EnchantsManager(this);
        this.api = new WildPrisonEnchantsAPIImpl(enchantsManager);
        this.loadMessages();

        Schedulers.async().runRepeating(() -> {
            Players.all().stream().forEach(player -> {
                ItemStack inHand = player.getItemInHand();
                ItemStack lastEquipped = currentPickaxes.get(player.getUniqueId());

                if (lastEquipped == null && inHand != null && inHand.getType() == Material.DIAMOND_PICKAXE && !player.getWorld().getName().equalsIgnoreCase("pvp")) {
                    currentPickaxes.put(player.getUniqueId(), inHand);
                    Schedulers.sync().run(() -> this.enchantsManager.handlePickaxeEquip(player, inHand));
                    return;
                }

                if (lastEquipped != null) {
                    if (inHand != null && inHand.getType() == Material.DIAMOND_PICKAXE) {
                        //Check if they are not the same
                        if (!areItemsSame(lastEquipped, inHand)) {
                            Schedulers.sync().run(() -> {
                                this.enchantsManager.handlePickaxeUnequip(player, lastEquipped);
                                this.enchantsManager.handlePickaxeEquip(player, inHand);
                            });
                            currentPickaxes.put(player.getUniqueId(), inHand);
                        }
                    } else if (inHand == null || inHand.getType() != Material.DIAMOND_PICKAXE) {
                        Schedulers.sync().run(() -> this.enchantsManager.handlePickaxeUnequip(player, lastEquipped));
                        currentPickaxes.remove(player.getUniqueId());
                    }
                }
            });
        }, 20, 20);
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
                    if (LuckyBoosterEnchant.hasLuckyBoosterRunning(c.sender())) {
                        c.sender().sendMessage(getMessage("lucky_mode_timeleft").replace("%timeleft%", LuckyBoosterEnchant.getTimeLeft(c.sender())));
                    } else {
                        c.sender().sendMessage(getMessage("lucky_mode_disabled"));
                    }
                }).registerAndBind(core, "luckybooster");

        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    ItemStack pickAxe = c.sender().getItemInHand();

                    if (pickAxe == null || pickAxe.getType() != Material.DIAMOND_PICKAXE) {
                        c.sender().sendMessage(getMessage("no_pickaxe_found"));
                        return;
                    }

                    c.sender().setItemInHand(null);
                    new DisenchantGUI(c.sender(), pickAxe).open();
                }).registerAndBind(core, "disenchant", "dise", "de");
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
        Commands.create()
                .assertPlayer()
                .assertPermission("wildprison.value", this.getMessage("value_no_permission"))
                .handler(c -> {
                    if (!valueCooldown.test(c.sender())) {
                        c.sender().sendMessage(this.getMessage("value_cooldown").replace("%time%", String.valueOf(valueCooldown.remainingTime(c.sender(), TimeUnit.SECONDS))));
                        return;
                    }
                    ItemStack pickAxe = c.sender().getItemInHand();

                    if (pickAxe == null || pickAxe.getType() != Material.DIAMOND_PICKAXE) {
                        c.sender().sendMessage(getMessage("value_no_pickaxe"));
                        return;
                    }

                    Players.all().forEach(p -> p.sendMessage(this.getMessage("value_value").replace("%player%", c.sender().getName()).replace("%tokens%", String.format("%,d", this.enchantsManager.getPickaxeValue(pickAxe)))));

                }).registerAndBind(core, "value");
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
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getItem() != null && e.getItem().getType() == Material.DIAMOND_PICKAXE && e.getAction() == Action.RIGHT_CLICK_AIR)
                .handler(e -> {
                    ItemStack pickAxe = e.getItem();
                    e.getPlayer().setItemInHand(null);
                    new EnchantGUI(e.getPlayer(), pickAxe).open();
                }).bindWith(core);
        /*Events.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getCurrentItem() != null && e.getWhoClicked().getItemInHand() != null && e.getCurrentItem().getType() == Material.DIAMOND_PICKAXE && e.getCurrentItem().equals(e.getWhoClicked().getItemInHand()))
                .handler(e -> {
                    if (e.getWhoClicked().getItemInHand().getType() == Material.DIAMOND_PICKAXE) {
                        this.enchantsManager.handlePickaxeUnequip((Player) e.getWhoClicked(), e.getCurrentItem());
                    }
                }).bindWith(core);
        Events.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getCursor() != null && e.getCursor().getType() == Material.DIAMOND_PICKAXE)
                .handler(e -> {
                    Schedulers.sync().runLater(() -> {
                        if (e.getWhoClicked().getItemInHand().equals(e.getCursor())) {
                            this.enchantsManager.handlePickaxeEquip((Player) e.getWhoClicked(), e.getCursor());
                        }
                    }, 5l);
                }).bindWith(core);
        Events.subscribe(PlayerDropItemEvent.class)
                .filter(e -> e.getItemDrop().getItemStack() != null && e.getItemDrop().getItemStack().getType() == Material.DIAMOND_PICKAXE && !e.getPlayer().getWorld().getName().equalsIgnoreCase("pvp"))
                .handler(e -> {
                    ItemStack dropped = e.getItemDrop().getItemStack();
                    enchantsManager.handlePickaxeUnequip(e.getPlayer(), dropped);
                }).bindWith(this.core);
        Events.subscribe(PlayerPickupItemEvent.class)
                .filter(e -> e.getItem().getItemStack() != null && e.getItem().getItemStack().getType() == Material.DIAMOND_PICKAXE && !e.getPlayer().getWorld().getName().equalsIgnoreCase("pvp"))
                .handler(e -> {
                    ItemStack equipped = e.getItem().getItemStack();
                    Schedulers.sync().runLater(() -> {
                        if (e.getPlayer().getItemInHand().equals(equipped)) {
                            this.enchantsManager.handlePickaxeEquip(e.getPlayer(), equipped);
                        }
                    }, 5l);
                }).bindWith(this.core);
        Events.subscribe(PlayerChangedWorldEvent.class)
                .filter(e -> e.getPlayer().getWorld().getName().equalsIgnoreCase("pvp"))
                .handler(e -> {
                    e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
                }).bindWith(this.core);
        Events.subscribe(PlayerItemHeldEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.getPlayer().getWorld().getName().equalsIgnoreCase("pvp"))
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
                }).bindWith(core);*/
        Events.subscribe(BlockBreakEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE && !e.getPlayer().getWorld().getName().equalsIgnoreCase("pvp") && !e.getPlayer().getWorld().getName().equalsIgnoreCase("plots"))
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

    private boolean areItemsSame(@NonNull ItemStack last, @NonNull ItemStack current) {
        List<String> loreLast = last.getItemMeta().getLore();
        List<String> loreCurrent = current.getItemMeta().getLore();

        if (loreLast == null || loreCurrent == null) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            loreLast.remove(0);
            loreCurrent.remove(0);
        }

        return loreLast.equals(loreCurrent);
    }

}
