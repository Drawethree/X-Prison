package me.drawethree.ultraprisoncore.enchants;

import lombok.Getter;
import lombok.NonNull;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPI;
import me.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPIImpl;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import me.drawethree.ultraprisoncore.enchants.gui.DisenchantGUI;
import me.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import me.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class UltraPrisonEnchants implements UltraPrisonModule {

    @Getter
    private static UltraPrisonEnchants instance;
    private final HashMap<UUID, ItemStack> currentPickaxes = new HashMap<>();

    @Getter
    private UltraPrisonEnchantsAPI api;

    @Getter
    private EnchantsManager enchantsManager;

    @Getter
    private FileManager.Config config;

    @Getter
    private UltraPrisonCore core;

    private HashMap<String, String> messages;
    private List<UUID> disabledLayer = new ArrayList<>();
    private List<UUID> disabledExplosive = new ArrayList<>();
    private CooldownMap<Player> valueCooldown = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));
    private boolean enabled;

    public UltraPrisonEnchants(UltraPrisonCore UltraPrisonCore) {
        instance = this;
        this.core = UltraPrisonCore;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {

        this.config.reload();


        this.loadMessages();
        this.enchantsManager.reload();

        EnchantGUI.reload();
        DisenchantGUI.reload();

        UltraPrisonEnchantment.reloadAll();
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, Text.colorize(getConfig().get().getString("messages." + key)));
        }
    }

    @Override
    public void enable() {
        this.enabled = true;

        this.config = this.core.getFileManager().getConfig("enchants.yml").copyDefaults(true).save();

        this.enchantsManager = new EnchantsManager(this);
        this.api = new UltraPrisonEnchantsAPIImpl(enchantsManager);
        this.loadMessages();

        Schedulers.async().runRepeating(() -> {
            Players.all().stream().forEach(player -> {
                ItemStack inHand = player.getItemInHand();
                ItemStack lastEquipped = currentPickaxes.get(player.getUniqueId());

                if (lastEquipped == null && inHand != null && inHand.getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial() && !player.getWorld().getName().equalsIgnoreCase("pvp")) {
                    currentPickaxes.put(player.getUniqueId(), inHand);
                    Schedulers.sync().run(() -> this.enchantsManager.handlePickaxeEquip(player, inHand));
                    return;
                }

                if (lastEquipped != null) {
                    if (inHand != null && inHand.getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial()) {
                        //Check if they are not the same
                        if (!areItemsSame(lastEquipped, inHand)) {
                            Schedulers.sync().run(() -> {
                                this.enchantsManager.handlePickaxeUnequip(player, lastEquipped);
                                this.enchantsManager.handlePickaxeEquip(player, inHand);
                            });
                            currentPickaxes.put(player.getUniqueId(), inHand);
                        }
                    } else if (inHand == null || inHand.getType() != CompMaterial.DIAMOND_PICKAXE.toMaterial()) {
                        Schedulers.sync().run(() -> this.enchantsManager.handlePickaxeUnequip(player, lastEquipped));
                        currentPickaxes.remove(player.getUniqueId());
                    }
                }
            });
        }, 20, 20);
        this.registerCommands();
        this.registerEvents();
    }

    @Override
    public void disable() {
        this.enabled = false;

        for (Player p : Players.all()) {
            p.closeInventory();
        }

    }

    @Override
    public String getName() {
        return "Enchants";
    }

    private void registerCommands() {


        Commands.create()
                .assertOp()
                .handler(c -> {

                    if (c.args().size() == 0) {
                        c.sender().sendMessage(Text.colorize("&c/givepickaxe <player> <enchant:<id>=<level>>,..."));
                        return;
                    }

                    String input = null;
                    Player target = null;

                    if (c.args().size() == 1) {
                        input = c.rawArg(0);
                    } else if (c.args().size() == 2) {
                        target = c.arg(0).parseOrFail(Player.class);
                        input = c.rawArg(1);
                    }

                    this.enchantsManager.givePickaxe(target, input, c.sender());
                }).registerAndBind(core, "givepickaxe");

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

                    if (pickAxe == null || pickAxe.getType() != CompMaterial.DIAMOND_PICKAXE.toMaterial()) {
                        c.sender().sendMessage(getMessage("no_pickaxe_found"));
                        return;
                    }

                    c.sender().setItemInHand(null);
                    new DisenchantGUI(c.sender(), pickAxe).open();
                }).registerAndBind(core, "disenchant", "dise", "de", "disenchantmenu", "dismenu");

        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    ItemStack pickAxe = c.sender().getItemInHand();

                    if (pickAxe == null || pickAxe.getType() != CompMaterial.DIAMOND_PICKAXE.toMaterial()) {
                        c.sender().sendMessage(getMessage("no_pickaxe_found"));
                        return;
                    }

                    c.sender().setItemInHand(null);
                    new EnchantGUI(c.sender(), pickAxe).open();
                }).registerAndBind(core, "enchantmenu", "enchmenu");

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
                        toggleLayer(c.sender());
                    }
                }).registerAndBind(core, "layer");
        Commands.create()
                .assertPlayer()
                .assertPermission("ultraprison.value", this.getMessage("value_no_permission"))
                .handler(c -> {
                    if (!valueCooldown.test(c.sender())) {
                        c.sender().sendMessage(this.getMessage("value_cooldown").replace("%time%", String.valueOf(valueCooldown.remainingTime(c.sender(), TimeUnit.SECONDS))));
                        return;
                    }
                    ItemStack pickAxe = c.sender().getItemInHand();

                    if (pickAxe == null || pickAxe.getType() != CompMaterial.DIAMOND_PICKAXE.toMaterial()) {
                        c.sender().sendMessage(getMessage("value_no_pickaxe"));
                        return;
                    }

                    Players.all().forEach(p -> p.sendMessage(this.getMessage("value_value").replace("%player%", c.sender().getName()).replace("%tokens%", String.format("%,d", this.enchantsManager.getPickaxeValue(pickAxe)))));

                }).registerAndBind(core, "value");
    }

    private void toggleLayer(Player sender) {
        if (disabledLayer.contains(sender.getUniqueId())) {
            sender.sendMessage(getMessage("layer_enabled"));
            disabledLayer.remove(sender.getUniqueId());
        } else {
            sender.sendMessage(getMessage("layer_disabled"));
            disabledLayer.add(sender.getUniqueId());
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
                .filter(e -> e.getItem() != null && e.getItem().getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial())
                .filter(e -> (e.getAction() == Action.RIGHT_CLICK_AIR || (e.getAction() == Action.RIGHT_CLICK_BLOCK && this.enchantsManager.isOpenEnchantMenuOnRightClickBlock())))
                .handler(e -> {
                    e.setCancelled(true);
                    ItemStack pickAxe = e.getItem();
                    e.getPlayer().setItemInHand(null);
                    new EnchantGUI(e.getPlayer(), pickAxe).open();
                }).bindWith(core);
        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial())
                .filter(e -> this.enchantsManager.isAllowEnchantsOutside() || WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch(region -> region.getId().toLowerCase().startsWith("mine")))
                .handler(e -> {
                    enchantsManager.addBlocksBrokenToItem(e.getPlayer(), 1);
                    enchantsManager.handleBlockBreak(e, e.getPlayer().getItemInHand());
                }).bindWith(core);
        Events.subscribe(BlockBreakEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial())
                .filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().noneMatch(region -> region.getId().toLowerCase().startsWith("mine")))
                .filter(e-> this.enchantsManager.hasEnchants(e.getPlayer().getItemInHand()))
                .handler(e -> e.setCancelled(true)).bindWith(core);

    }

    public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), Text.colorize("&cMessage " + key + " not found."));
    }

    public boolean hasLayerDisabled(Player p) {
        return disabledLayer.contains(p.getUniqueId());
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
            try {
                loreLast.remove(0);
                loreCurrent.remove(0);
            } catch (Exception e) {
                break;
            }
        }

        return loreLast.equals(loreCurrent);
    }

}
