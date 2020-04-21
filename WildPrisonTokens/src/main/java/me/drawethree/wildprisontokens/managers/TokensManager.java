package me.drawethree.wildprisontokens.managers;

import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.drawethree.wildprisontokens.database.MySQLDatabase;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class TokensManager {

    private WildPrisonTokens plugin;

    public TokensManager(WildPrisonTokens plugin) {
        this.plugin = plugin;

        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> {
                    this.addIntoTable(e.getPlayer());
                }).bindWith(plugin);
    }

    private void addIntoTable(Player player) {
        Schedulers.async().run(() -> {
            ResultSet set = this.plugin.getSqlDatabase().query("SELECT * FROM " + MySQLDatabase.TOKENS_DB_NAME + " WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", player.getUniqueId().toString());
            try {
                if (set.next()) {
                    return;
                } else {
                    this.plugin.getSqlDatabase().execute("INSERT INTO " + MySQLDatabase.TOKENS_DB_NAME + "(" + MySQLDatabase.TOKENS_UUID_COLNAME + "," + MySQLDatabase.TOKENS_TOKENS_COLNAME + ") VALUES(?,?)", player.getUniqueId().toString(), 0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void setTokens(OfflinePlayer p, long newAmount, CommandSender executor) {
        Schedulers.async().run(() -> {
            this.plugin.getSqlDatabase().execute("UPDATE " + MySQLDatabase.TOKENS_DB_NAME + " SET " + MySQLDatabase.TOKENS_TOKENS_COLNAME + "=? WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", newAmount, p.getPlayer().getUniqueId().toString());
            executor.sendMessage(WildPrisonTokens.getMessage("admin_set_tokens").replace("%player%", p.getName()).replace("%tokens%", String.valueOf(newAmount)));
        });
    }

    public void giveTokens(OfflinePlayer p, long amount, CommandSender executor) {
        Schedulers.async().run(() -> {
            long currentTokens = getPlayerTokens(p);
            this.plugin.getSqlDatabase().execute("UPDATE " + MySQLDatabase.TOKENS_DB_NAME + " SET " + MySQLDatabase.TOKENS_TOKENS_COLNAME + "=? WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", amount + currentTokens, p.getPlayer().getUniqueId().toString());
            if (executor != null) {
                executor.sendMessage(WildPrisonTokens.getMessage("admin_give_tokens").replace("%player%", p.getName()).replace("%tokens%", String.valueOf(amount)));
            }
        });
    }

    public void redeemTokens(Player p, ItemStack item) {
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        displayName = displayName.replace(" TOKENS", "");
        try {
            int amount = Integer.parseInt(displayName);
            this.giveTokens(p,amount,null);
            if (item.getAmount() == 1) {
                p.getInventory().remove(item);
            } else {
                item.setAmount(item.getAmount() - 1);
            }
            p.sendMessage(WildPrisonTokens.getMessage("tokens_redeem").replace("%tokens%", String.valueOf(amount)));
        } catch (Exception e) {
            //Not a token item
            p.sendMessage(WildPrisonTokens.getMessage("not_token_item"));
            return;
        }
    }

    public void payTokens(Player executor, long amount, OfflinePlayer target) {
        Schedulers.async().run(() -> {
            if(getPlayerTokens(executor) >= amount) {
                this.removeTokens(executor, amount, null);
                this.giveTokens(target, amount, null);
                executor.sendMessage(WildPrisonTokens.getMessage("tokens_send").replace("%player%", target.getName()).replace("%tokens%", String.valueOf(amount)));
                if(target.isOnline()) {
                    ((Player)target).sendMessage(WildPrisonTokens.getMessage("tokens_received").replace("%player%", executor.getName()).replace("%tokens%", String.valueOf(amount)));
                }
            } else {
                executor.sendMessage(WildPrisonTokens.getMessage("not_enough_tokens"));
            }
        });
    }

    public void withdrawTokens(Player executor, long amount, int value) {
        Schedulers.async().run(() -> {
            long totalAmount = amount * value;

            if (this.getPlayerTokens(executor) < totalAmount) {
                executor.sendMessage(WildPrisonTokens.getMessage("not_enough_tokens"));
                return;
            }

            removeTokens(executor, totalAmount, null);

            ItemStack item = createTokenItem(amount, value);
            executor.getInventory().addItem(item);

            executor.sendMessage(WildPrisonTokens.getMessage("withdraw_successful").replace("%amount%", String.valueOf(amount)).replace("%value%", String.valueOf(value)));
        });
    }

    public long getPlayerTokens(OfflinePlayer p) {
        ResultSet set = plugin.getSqlDatabase().query("SELECT * FROM " + MySQLDatabase.TOKENS_DB_NAME + " WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", p.getUniqueId().toString());
        try {
            if (set.next()) {
                return set.getLong(MySQLDatabase.TOKENS_TOKENS_COLNAME);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void removeTokens(OfflinePlayer p, long amount, CommandSender executor) {
        Schedulers.async().run(() -> {
            long currentTokens = getPlayerTokens(p);
            long finalTokens = currentTokens - amount;

            if (finalTokens < 0) {
                finalTokens = 0;
            }

            this.plugin.getSqlDatabase().execute("UPDATE " + MySQLDatabase.TOKENS_DB_NAME + " SET " + MySQLDatabase.TOKENS_TOKENS_COLNAME + "=? WHERE " + MySQLDatabase.TOKENS_UUID_COLNAME + "=?", finalTokens, p.getPlayer().getUniqueId().toString());

            if (executor != null) {
                executor.sendMessage(WildPrisonTokens.getMessage("admin_remove_tokens").replace("%player%", p.getName()).replace("%tokens%", String.valueOf(amount)));
            }
        });
    }

    public static ItemStack createTokenItem(long amount, int value) {
        return ItemStackBuilder.of(Material.DOUBLE_PLANT).amount(value).name("&e&l" + amount + " TOKENS").lore("&7Right-Click to Redeem").enchant(Enchantment.PROTECTION_ENVIRONMENTAL).flag(ItemFlag.HIDE_ENCHANTS).build();
    }

    public void sendInfoMessage(CommandSender sender, OfflinePlayer target) {
        Schedulers.async().run(() -> {
            if (sender == target) {
                sender.sendMessage(WildPrisonTokens.getMessage("your_tokens").replace("%tokens%", String.valueOf(this.getPlayerTokens(target))));
            } else {
                sender.sendMessage(WildPrisonTokens.getMessage("other_tokens").replace("%tokens%", String.valueOf(this.getPlayerTokens(target))).replace("%player%", target.getName()));
            }
        });
    }
}
