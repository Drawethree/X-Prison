package me.drawethree.ultraprisoncore.gems.managers;

import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.api.events.player.UltraPrisonPlayerGemsReceiveEvent;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GemsManager {


    private UltraPrisonGems plugin;
    private String SPACER_LINE;
    private String SPACER_LINE_BOTTOM;
    private String TOP_FORMAT_GEMS;
    private HashMap<UUID, Long> gemsCache = new HashMap<>();
    private LinkedHashMap<UUID, Long> top10Gems = new LinkedHashMap<>();
    private Task task;
    private boolean updating;

    public GemsManager(UltraPrisonGems plugin) {
        this.plugin = plugin;
        this.reload();

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    this.addIntoTable(e.getPlayer());
                    this.loadPlayerData(e.getPlayer());
                }).bindWith(plugin.getCore());
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> {
                    this.savePlayerData(e.getPlayer(), true, true);
                    e.getPlayer().getActivePotionEffects().forEach(effect -> e.getPlayer().removePotionEffect(effect.getType()));
                }).bindWith(plugin.getCore());

        this.loadPlayerDataOnEnable();
        this.updateTop10();
    }

    public void reload() {
        this.SPACER_LINE = plugin.getMessage("top_spacer_line");
        this.SPACER_LINE_BOTTOM = plugin.getMessage("top_spacer_line_bottom");
        this.TOP_FORMAT_GEMS = plugin.getMessage("top_format_gems");
    }

    public void stopUpdating() {
        this.plugin.getCore().getLogger().info("Stopping updating Top 10 - Gems");
        task.close();
    }

    private void updateTop10() {
        this.updating = true;
        task = Schedulers.async().runRepeating(() -> {
            this.updating = true;
            Players.all().forEach(p -> savePlayerData(p, false, false));
            this.updateGemsTop();
            this.updating = false;
        }, 1, TimeUnit.MINUTES, 1, TimeUnit.HOURS);
    }

    private void savePlayerData(Player player, boolean removeFromCache, boolean async) {
        if (async) {
            Schedulers.async().run(() -> {
				this.plugin.getCore().getPluginDatabase().updateGems(player, gemsCache.getOrDefault(player.getUniqueId(), 0L));
                if (removeFromCache) {
                    gemsCache.remove(player.getUniqueId());
                }
                this.plugin.getCore().getLogger().info(String.format("Saved data of player %s to database.", player.getName()));
            });
        } else {
			this.plugin.getCore().getPluginDatabase().updateGems(player, gemsCache.getOrDefault(player.getUniqueId(), 0L));
            if (removeFromCache) {
                gemsCache.remove(player.getUniqueId());
            }
            this.plugin.getCore().getLogger().info(String.format("Saved data of player %s to database.", player.getName()));
        }
    }

    public void savePlayerDataOnDisable() {
        this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saving all player data - gems");
        Schedulers.sync().run(() -> {
            for (UUID uuid : gemsCache.keySet()) {
				this.plugin.getCore().getPluginDatabase().updateGems(Players.getOfflineNullable(uuid), gemsCache.getOrDefault(uuid, 0L));
            }
            gemsCache.clear();
            this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saved all player data to database - gems");
        });
    }

    private void addIntoTable(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getCore().getPluginDatabase().addIntoGems(player);
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(p -> loadPlayerData(p));
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> {
            long playerGems = this.plugin.getCore().getPluginDatabase().getPlayerGems(player);
            gemsCache.put(player.getUniqueId(), playerGems);
            this.plugin.getCore().getLogger().info(String.format("Loaded gems of player %s from database", player.getName()));
        });
    }

    public void setGems(OfflinePlayer p, long newAmount, CommandSender executor) {
        Schedulers.async().run(() -> {
            if (!p.isOnline()) {
                this.plugin.getCore().getPluginDatabase().updateGems(p, newAmount);
            } else {
                gemsCache.put(p.getUniqueId(), newAmount);
            }
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_set_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", newAmount)));
        });
    }

    public void giveGems(OfflinePlayer p, long amount, CommandSender executor, ReceiveCause cause) {
        long currentgems = getPlayerGems(p);

        UltraPrisonPlayerGemsReceiveEvent event = new UltraPrisonPlayerGemsReceiveEvent(cause, p, amount);

        Events.callSync(event);

        if (event.isCancelled()) {
            return;
        }

        long finalAmount = event.getAmount();


        if (!p.isOnline()) {
            this.plugin.getCore().getPluginDatabase().updateGems(p, currentgems + finalAmount);
        } else {
            gemsCache.put(p.getUniqueId(), gemsCache.getOrDefault(p.getUniqueId(), (long) 0) + finalAmount);
        }
        if (executor != null && !(executor instanceof ConsoleCommandSender)) {
			PlayerUtils.sendMessage(executor, plugin.getMessage("admin_give_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", finalAmount)));
        }

    }

    public void redeemGems(Player p, ItemStack item, boolean shiftClick) {
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        displayName = displayName.replace(" gems", "").replace(",", "");
        try {
            long tokenAmount = Long.parseLong(displayName);
            int itemAmount = item.getAmount();
            if (shiftClick) {
                p.setItemInHand(null);
                this.giveGems(p, tokenAmount * itemAmount, null, ReceiveCause.REDEEM);
				PlayerUtils.sendMessage(p, plugin.getMessage("gems_redeem").replace("%gems%", String.format("%,d", tokenAmount * itemAmount)));
            } else {
                this.giveGems(p, tokenAmount, null, ReceiveCause.REDEEM);
                if (item.getAmount() == 1) {
                    p.setItemInHand(null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
				PlayerUtils.sendMessage(p, plugin.getMessage("gems_redeem").replace("%gems%", String.format("%,d", tokenAmount)));
            }
        } catch (Exception e) {
            //Not a token item
			PlayerUtils.sendMessage(p, plugin.getMessage("not_gems_item"));
        }
    }

    public void payGems(Player executor, long amount, OfflinePlayer target) {
        Schedulers.async().run(() -> {
            if (getPlayerGems(executor) >= amount) {
                this.removeGems(executor, amount, null);
                this.giveGems(target, amount, null, ReceiveCause.PAY);
				PlayerUtils.sendMessage(executor, plugin.getMessage("gems_send").replace("%player%", target.getName()).replace("%gems%", String.format("%,d", amount)));
                if (target.isOnline()) {
					PlayerUtils.sendMessage((CommandSender) target, plugin.getMessage("gems_received").replace("%player%", executor.getName()).replace("%gems%", String.format("%,d", amount)));
                }
            } else {
				PlayerUtils.sendMessage(executor, plugin.getMessage("not_enough_gems"));
            }
        });
    }

    public void withdrawGems(Player executor, long amount, int value) {
        Schedulers.async().run(() -> {
            long totalAmount = amount * value;

            if (this.getPlayerGems(executor) < totalAmount) {
				PlayerUtils.sendMessage(executor, plugin.getMessage("not_enough_gems"));
                return;
            }

            removeGems(executor, totalAmount, null);

            ItemStack item = createGemsItem(amount, value);
            Collection<ItemStack> notFit = executor.getInventory().addItem(item).values();

            if (!notFit.isEmpty()) {
                notFit.forEach(itemStack -> {
                    this.giveGems(executor, amount * item.getAmount(), null, ReceiveCause.REDEEM);
                });
            }

			PlayerUtils.sendMessage(executor, plugin.getMessage("withdraw_successful").replace("%amount%", String.format("%,d", amount)).replace("%value%", String.format("%,d", value)));
        });
    }

    public long getPlayerGems(OfflinePlayer p) {
        if (!p.isOnline()) {
            return this.plugin.getCore().getPluginDatabase().getPlayerGems(p);
        } else {
            return gemsCache.getOrDefault(p.getUniqueId(), (long) 0);
        }
    }

    public void removeGems(OfflinePlayer p, long amount, CommandSender executor) {
        Schedulers.async().run(() -> {
            long currentgems = getPlayerGems(p);
            long finalgems = currentgems - amount;

            if (finalgems < 0) {
                finalgems = 0;
            }

            if (!p.isOnline()) {
                this.plugin.getCore().getPluginDatabase().updateGems(p, finalgems);
            } else {
                gemsCache.put(p.getUniqueId(), finalgems);
            }
            if (executor != null) {
				PlayerUtils.sendMessage(executor, plugin.getMessage("admin_remove_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", amount)));
            }
        });
    }

    public static ItemStack createGemsItem(long amount, int value) {
        return ItemStackBuilder.of(CompMaterial.SUNFLOWER.toItem()).amount(value).name("&e&l" + String.format("%,d", amount) + " GEMS").lore("&7Right-Click to Redeem").enchant(Enchantment.PROTECTION_ENVIRONMENTAL).flag(ItemFlag.HIDE_ENCHANTS).build();
    }

    public void sendInfoMessage(CommandSender sender, OfflinePlayer target) {
        Schedulers.async().run(() -> {
            if (sender == target) {
				PlayerUtils.sendMessage(sender, plugin.getMessage("your_gems").replace("%gems%", String.format("%,d", this.getPlayerGems(target))));
            } else {
				PlayerUtils.sendMessage(sender, plugin.getMessage("other_gems").replace("%gems%", String.format("%,d", this.getPlayerGems(target))).replace("%player%", target.getName()));
            }
        });
    }


    private void updateGemsTop() {
        top10Gems = new LinkedHashMap<>();
        this.plugin.getCore().getLogger().info("Starting updating GemsTop");

        this.top10Gems = (LinkedHashMap<UUID, Long>) this.plugin.getCore().getPluginDatabase().getTop10Gems();
        this.plugin.getCore().getLogger().info("GemsTop updated!");
    }

    public void sendGemsTop(CommandSender sender) {
        Schedulers.async().run(() -> {
			PlayerUtils.sendMessage(sender, Text.colorize(SPACER_LINE));
            if (this.updating) {
				PlayerUtils.sendMessage(sender, this.plugin.getMessage("top_updating"));
				PlayerUtils.sendMessage(sender, Text.colorize(SPACER_LINE_BOTTOM));
                return;
            }
            for (int i = 0; i < 10; i++) {
                try {
                    UUID uuid = (UUID) top10Gems.keySet().toArray()[i];
                    OfflinePlayer player = Players.getOfflineNullable(uuid);
                    String name;
                    if (player.getName() == null) {
                        name = "Unknown Player";
                    } else {
                        name = player.getName();
                    }
                    long gems = top10Gems.get(uuid);
					PlayerUtils.sendMessage(sender, TOP_FORMAT_GEMS.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%amount%", String.format("%,d", gems)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }
			PlayerUtils.sendMessage(sender, Text.colorize(SPACER_LINE_BOTTOM));
        });
    }
}
