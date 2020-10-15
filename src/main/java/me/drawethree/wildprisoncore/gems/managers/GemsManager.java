package me.drawethree.wildprisoncore.gems.managers;

import me.drawethree.wildprisoncore.api.events.player.WildPrisonPlayerGemsReceiveEvent;
import me.drawethree.wildprisoncore.database.MySQLDatabase;
import me.drawethree.wildprisoncore.gems.WildPrisonGems;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GemsManager {


    private WildPrisonGems plugin;
    private String SPACER_LINE;
    private String TOP_FORMAT_GEMS;
    private HashMap<UUID, Long> gemsCache = new HashMap<>();
    private LinkedHashMap<UUID, Long> top10Gems = new LinkedHashMap<>();
    private Task task;
    private boolean updating;

    public GemsManager(WildPrisonGems plugin) {
        this.plugin = plugin;
        this.SPACER_LINE = plugin.getMessage("top_spacer_line");
        this.TOP_FORMAT_GEMS = plugin.getMessage("top_format_gems");

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
                this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GEMS_DB_NAME + " SET " + MySQLDatabase.GEMS_GEMS_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", gemsCache.get(player.getUniqueId()), player.getUniqueId().toString());
                if (removeFromCache) {
                    gemsCache.remove(player.getUniqueId());
                }
                this.plugin.getCore().getLogger().info(String.format("Saved data of player %s to database.", player.getName()));
            });
        } else {
            this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GEMS_DB_NAME + " SET " + MySQLDatabase.GEMS_GEMS_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", gemsCache.get(player.getUniqueId()), player.getUniqueId().toString());
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
                this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GEMS_DB_NAME + " SET " + MySQLDatabase.GEMS_GEMS_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", gemsCache.get(uuid), uuid.toString());
            }
            gemsCache.clear();
            this.plugin.getCore().getLogger().info("[PLUGIN DISABLE] Saved all player data to database - gems");
        });
    }

    private void addIntoTable(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getCore().getSqlDatabase().execute("INSERT IGNORE INTO " + MySQLDatabase.GEMS_DB_NAME + " VALUES(?,?)", player.getUniqueId().toString(), 0);
        });
    }

    private void loadPlayerDataOnEnable() {
        Players.all().forEach(p -> loadPlayerData(p));
    }

    private void loadPlayerData(Player player) {
        Schedulers.async().run(() -> {
            try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.GEMS_DB_NAME + " WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?")) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet gems = statement.executeQuery()) {
                    if (gems.next()) {
                        gemsCache.put(UUID.fromString(gems.getString(MySQLDatabase.GEMS_UUID_COLNAME)), gems.getLong(MySQLDatabase.GEMS_GEMS_COLNAME));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            this.plugin.getCore().getLogger().info(String.format("Loaded gems of player %s from database", player.getName()));
        });
    }

    public void setGems(OfflinePlayer p, long newAmount, CommandSender executor) {
        Schedulers.async().run(() -> {
            if (!p.isOnline()) {
                this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GEMS_DB_NAME + " SET " + MySQLDatabase.GEMS_UUID_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", newAmount, p.getUniqueId().toString());
            } else {
                gemsCache.put(p.getUniqueId(), newAmount);
            }
            executor.sendMessage(plugin.getMessage("admin_set_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", newAmount)));
        });
    }

    public void giveGems(OfflinePlayer p, long amount, CommandSender executor) {
        Schedulers.async().run(() -> {
            long currentgems = getPlayerGems(p);

            WildPrisonPlayerGemsReceiveEvent event = new WildPrisonPlayerGemsReceiveEvent(p, amount);

            Events.call(event);

            if (event.isCancelled()) {
                return;
            }

            long finalAmount = event.getAmount();

            if (!p.isOnline()) {
                this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GEMS_DB_NAME + " SET " + MySQLDatabase.GEMS_UUID_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", finalAmount + currentgems, p.getUniqueId().toString());
            } else {
                gemsCache.put(p.getUniqueId(), gemsCache.getOrDefault(p.getUniqueId(), (long) 0) + finalAmount);
            }
            if (executor != null) {
                executor.sendMessage(plugin.getMessage("admin_give_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", finalAmount)));
            }
        });
    }

    public void redeemGems(Player p, ItemStack item, boolean shiftClick) {
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        displayName = displayName.replace(" gems", "").replace(",", "");
        try {
            long tokenAmount = Long.parseLong(displayName);
            int itemAmount = item.getAmount();
            if (shiftClick) {
                p.setItemInHand(null);
                this.giveGems(p, tokenAmount * itemAmount, null);
                p.sendMessage(plugin.getMessage("gems_redeem").replace("%gems%", String.format("%,d", tokenAmount * itemAmount)));
            } else {
                this.giveGems(p, tokenAmount, null);
                if (item.getAmount() == 1) {
                    p.setItemInHand(null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                p.sendMessage(plugin.getMessage("gems_redeem").replace("%gems%", String.format("%,d", tokenAmount)));
            }
        } catch (Exception e) {
            //Not a token item
            p.sendMessage(plugin.getMessage("not_gems_item"));
        }
    }

    public void payGems(Player executor, long amount, OfflinePlayer target) {
        Schedulers.async().run(() -> {
            if (getPlayerGems(executor) >= amount) {
                this.removeGems(executor, amount, null);
                this.giveGems(target, amount, null);
                executor.sendMessage(plugin.getMessage("gems_send").replace("%player%", target.getName()).replace("%gems%", String.format("%,d", amount)));
                if (target.isOnline()) {
                    ((Player) target).sendMessage(plugin.getMessage("gems_received").replace("%player%", executor.getName()).replace("%gems%", String.format("%,d", amount)));
                }
            } else {
                executor.sendMessage(plugin.getMessage("not_enough_gems"));
            }
        });
    }

    public void withdrawGems(Player executor, long amount, int value) {
        Schedulers.async().run(() -> {
            long totalAmount = amount * value;

            if (this.getPlayerGems(executor) < totalAmount) {
                executor.sendMessage(plugin.getMessage("not_enough_gems"));
                return;
            }

            removeGems(executor, totalAmount, null);

            ItemStack item = createGemsItem(amount, value);
            Collection<ItemStack> notFit = executor.getInventory().addItem(item).values();

            if (!notFit.isEmpty()) {
                notFit.forEach(itemStack -> {
                    this.giveGems(executor, amount * item.getAmount(), null);
                });
            }

            executor.sendMessage(plugin.getMessage("withdraw_successful").replace("%amount%", String.format("%,d,", amount)).replace("%value%", String.format("%,d", value)));
        });
    }

    public long getPlayerGems(OfflinePlayer p) {
        if (!p.isOnline()) {
            try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.GEMS_DB_NAME + " WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?")) {
                statement.setString(1, p.getUniqueId().toString());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        return set.getLong(MySQLDatabase.GEMS_UUID_COLNAME);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            return gemsCache.getOrDefault(p.getUniqueId(), (long) 0);
        }
        return 0;
    }

    public void removeGems(OfflinePlayer p, long amount, CommandSender executor) {
        Schedulers.async().run(() -> {
            long currentgems = getPlayerGems(p);
            long finalgems = currentgems - amount;

            if (finalgems < 0) {
                finalgems = 0;
            }

            if (!p.isOnline()) {
                this.plugin.getCore().getSqlDatabase().execute("UPDATE " + MySQLDatabase.GEMS_DB_NAME + " SET " + MySQLDatabase.GEMS_UUID_COLNAME + "=? WHERE " + MySQLDatabase.GEMS_UUID_COLNAME + "=?", finalgems, p.getUniqueId().toString());
            } else {
                gemsCache.put(p.getUniqueId(), finalgems);
            }
            if (executor != null) {
                executor.sendMessage(plugin.getMessage("admin_remove_gems").replace("%player%", p.getName()).replace("%gems%", String.format("%,d", amount)));
            }
        });
    }

    public static ItemStack createGemsItem(long amount, int value) {
        return ItemStackBuilder.of(Material.DOUBLE_PLANT).amount(value).name("&e&l" + String.format("%,d", amount) + " GEMS").lore("&7Right-Click to Redeem").enchant(Enchantment.PROTECTION_ENVIRONMENTAL).flag(ItemFlag.HIDE_ENCHANTS).build();
    }

    public void sendInfoMessage(CommandSender sender, OfflinePlayer target) {
        Schedulers.async().run(() -> {
            if (sender == target) {
                sender.sendMessage(plugin.getMessage("your_gems").replace("%gems%", String.format("%,d", this.getPlayerGems(target))));
            } else {
                sender.sendMessage(plugin.getMessage("other_gems").replace("%gems%", String.format("%,d", this.getPlayerGems(target))).replace("%player%", target.getName()));
            }
        });
    }


    private void updateGemsTop() {
        top10Gems = new LinkedHashMap<>();
        this.plugin.getCore().getLogger().info("Starting updating GemssTop");
        try (Connection con = this.plugin.getCore().getSqlDatabase().getHikari().getConnection(); ResultSet set = con.prepareStatement("SELECT " + MySQLDatabase.GEMS_UUID_COLNAME + "," + MySQLDatabase.GEMS_GEMS_COLNAME + " FROM " + MySQLDatabase.GEMS_DB_NAME + " ORDER BY " + MySQLDatabase.GEMS_GEMS_COLNAME + " DESC LIMIT 10").executeQuery()) {
            while (set.next()) {
                top10Gems.put(UUID.fromString(set.getString(MySQLDatabase.GEMS_UUID_COLNAME)), set.getLong(MySQLDatabase.GEMS_GEMS_COLNAME));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.plugin.getCore().getLogger().info("GemsTop updated!");
    }

    public void sendGemsTop(CommandSender sender) {
        Schedulers.async().run(() -> {
            sender.sendMessage(Text.colorize(SPACER_LINE));
            if (this.updating) {
                sender.sendMessage(this.plugin.getMessage("top_updating"));
                sender.sendMessage(Text.colorize(SPACER_LINE));
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
                    sender.sendMessage(TOP_FORMAT_GEMS.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%amount%", String.format("%,d", gems)));
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }
            sender.sendMessage(Text.colorize(SPACER_LINE));
        });
    }
}
