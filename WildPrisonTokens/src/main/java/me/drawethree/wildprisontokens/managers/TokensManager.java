package me.drawethree.wildprisontokens.managers;

import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.drawethree.wildprisontokens.database.MySQLDatabase;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TokensManager {


    private static String SPACER_LINE = WildPrisonTokens.getMessage("top_spacer_line");
    private static String TOP_FORMAT_BLOCKS = WildPrisonTokens.getMessage("top_format_blocks");
    private static String TOP_FORMAT_TOKENS = WildPrisonTokens.getMessage("top_format_tokens");

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
                if (!set.next()) {
                    this.plugin.getSqlDatabase().execute("INSERT INTO " + MySQLDatabase.TOKENS_DB_NAME + "(" + MySQLDatabase.TOKENS_UUID_COLNAME + "," + MySQLDatabase.TOKENS_TOKENS_COLNAME + ") VALUES(?,?)", player.getUniqueId().toString(), 0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            set = this.plugin.getSqlDatabase().query("SELECT * FROM " + MySQLDatabase.BLOCKS_DB_NAME + " WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", player.getUniqueId().toString());
            try {
                if (!set.next()) {
                    this.plugin.getSqlDatabase().execute("INSERT INTO " + MySQLDatabase.BLOCKS_DB_NAME + "(" + MySQLDatabase.BLOCKS_UUID_COLNAME + "," + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + ") VALUES(?,?)", player.getUniqueId().toString(), 0);
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
            this.giveTokens(p, amount, null);
            if (item.getAmount() == 1) {
                p.setItemInHand(null);
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
            if (getPlayerTokens(executor) >= amount) {
                this.removeTokens(executor, amount, null);
                this.giveTokens(target, amount, null);
                executor.sendMessage(WildPrisonTokens.getMessage("tokens_send").replace("%player%", target.getName()).replace("%tokens%", String.valueOf(amount)));
                if (target.isOnline()) {
                    ((Player) target).sendMessage(WildPrisonTokens.getMessage("tokens_received").replace("%player%", executor.getName()).replace("%tokens%", String.valueOf(amount)));
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

    public long getPlayerBrokenBlocks(OfflinePlayer p) {
        ResultSet set = plugin.getSqlDatabase().query("SELECT * FROM " + MySQLDatabase.BLOCKS_DB_NAME + " WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", p.getUniqueId().toString());
        try {
            if (set.next()) {
                return set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME);
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

    public void sendInfoMessage(CommandSender sender, OfflinePlayer target, boolean tokens) {
        Schedulers.async().run(() -> {
            if (sender == target) {
                if (tokens) {
                    sender.sendMessage(WildPrisonTokens.getMessage("your_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))));
                } else {
                    sender.sendMessage(WildPrisonTokens.getMessage("your_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))));
                }
            } else {
                if (tokens) {
                    sender.sendMessage(WildPrisonTokens.getMessage("other_tokens").replace("%tokens%", String.format("%,d", this.getPlayerTokens(target))).replace("%player%", target.getName()));
                } else {
                    sender.sendMessage(WildPrisonTokens.getMessage("other_blocks").replace("%blocks%", String.format("%,d", this.getPlayerBrokenBlocks(target))).replace("%player%", target.getName()));
                }
            }
        });
    }

    public void addBlocksBroken(Player player, int amount) {
        Schedulers.async().run(() -> {
            long currentBroken = getPlayerBrokenBlocks(player);
            this.plugin.getSqlDatabase().execute("UPDATE " + MySQLDatabase.BLOCKS_DB_NAME + " SET " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + "=? WHERE " + MySQLDatabase.BLOCKS_UUID_COLNAME + "=?", currentBroken + amount, player.getUniqueId().toString());
        });
    }

    public void sendBlocksTop(CommandSender sender) {
        Schedulers.async().run(() -> {
            ResultSet set = this.plugin.getSqlDatabase().query("SELECT " + MySQLDatabase.BLOCKS_UUID_COLNAME + "," + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " FROM " + MySQLDatabase.BLOCKS_DB_NAME + " ORDER BY " + MySQLDatabase.BLOCKS_BLOCKS_COLNAME + " LIMIT 10");
            sender.sendMessage(Text.colorize(SPACER_LINE));
            for (int i = 0; i < 10; i++) {
                try {
                    if (!set.next()) {
                        break;
                    }
                    sender.sendMessage(TOP_FORMAT_BLOCKS.replace("%position%", String.valueOf(i + 1)).replace("%player%", Players.getOfflineNullable(UUID.fromString(set.getString(MySQLDatabase.BLOCKS_UUID_COLNAME))).getName()).replace("%amount%", String.format("%,d", set.getLong(MySQLDatabase.BLOCKS_BLOCKS_COLNAME))));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            sender.sendMessage(Text.colorize(SPACER_LINE));
        });
    }

    public void sendTokensTop(CommandSender sender) {
        Schedulers.async().run(() -> {
            ResultSet set = this.plugin.getSqlDatabase().query("SELECT " + MySQLDatabase.TOKENS_UUID_COLNAME + "," + MySQLDatabase.TOKENS_TOKENS_COLNAME + " FROM " + MySQLDatabase.TOKENS_DB_NAME + " ORDER BY " + MySQLDatabase.TOKENS_TOKENS_COLNAME + " DESC LIMIT 10");
            sender.sendMessage(Text.colorize(SPACER_LINE));
            for (int i = 0; i < 10; i++) {
                try {
                    if (!set.next()) {
                        break;
                    }
                    sender.sendMessage(TOP_FORMAT_TOKENS.replace("%position%", String.valueOf(i + 1)).replace("%player%", Players.getOfflineNullable(UUID.fromString(set.getString(MySQLDatabase.TOKENS_UUID_COLNAME))).getName()).replace("%amount%", String.format("%,d", set.getLong(MySQLDatabase.TOKENS_TOKENS_COLNAME))));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            sender.sendMessage(Text.colorize(SPACER_LINE));

        });
    }

}
