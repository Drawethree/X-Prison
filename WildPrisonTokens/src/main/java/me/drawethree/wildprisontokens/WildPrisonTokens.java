package me.drawethree.wildprisontokens;


import lombok.Getter;
import me.drawethree.wildprisontokens.api.WildPrisonTokensAPI;
import me.drawethree.wildprisontokens.api.WildPrisonTokensAPIImpl;
import me.drawethree.wildprisontokens.commands.TokensCommand;
import me.drawethree.wildprisontokens.database.MySQLDatabase;
import me.drawethree.wildprisontokens.managers.TokensManager;
import me.drawethree.wildprisontokens.placeholders.WildPrisonPlaceholder;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class WildPrisonTokens extends ExtendedJavaPlugin {

    public static final String TOKENS_ADMIN_PERM = "wildprison.tokens.admin";

    @Getter
    private static WildPrisonTokens instance;
    @Getter
    private static WildPrisonTokensAPI api;

    private static HashMap<String, String> messages;

    @Getter
    private TokensManager tokensManager;
    @Getter
    private MySQLDatabase sqlDatabase;

    private double chance;
    private long minAmount;
    private long maxAmount;

    @Override
    protected void load() {
        instance = this;
        saveDefaultConfig();
        this.loadMessages();
        this.loadVariables();
    }

    private void loadVariables() {
        this.chance = getConfig().getDouble("tokens.breaking.chance");
        this.minAmount = getConfig().getLong("tokens.breaking.min");
        this.maxAmount = getConfig().getLong("tokens.breaking.max");
    }

    @Override
    protected void enable() {
        this.tokensManager = new TokensManager(this);
        this.sqlDatabase = new MySQLDatabase(this);
        api = new WildPrisonTokensAPIImpl(this.tokensManager);
        this.registerCommands();
        this.registerEvents();
        this.registerPlaceholders();
    }

    @Override
    protected void disable() {

    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getItem() != null && e.getItem().getType() == Material.DOUBLE_PLANT && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
                .handler(e -> {
                    if (e.getItem().hasItemMeta()) {
                        this.tokensManager.redeemTokens(e.getPlayer(), e.getItem());
                    }
                })
                .bindWith(this);
        Events.subscribe(BlockBreakEvent.class)
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE)
                .handler(e -> {
                    tokensManager.addBlocksBroken(e.getPlayer(),1);
                    if (chance >= ThreadLocalRandom.current().nextDouble()) {
                        long randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
                        tokensManager.giveTokens(e.getPlayer(), randAmount, null);
                    }
                }).bindWith(this);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new WildPrisonPlaceholder(this).register();
        }
    }

    private void registerCommands() {
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.tokensManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender(),true);
                        return;
                    }
                    TokensCommand subCommand = TokensCommand.getCommand(c.rawArg(0));
                    if (subCommand != null) {
                        subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                    } else {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.tokensManager.sendInfoMessage(c.sender(), target,true);
                    }
                })
                .registerAndBind(this, "tokens");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendBlocksTop(c.sender());
                    }
                })
                .registerAndBind(this, "blockstop", "blocktop");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendTokensTop(c.sender());
                    }
                })
                .registerAndBind(this, "tokenstop", "tokentop");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender(), false);
                    } else if (c.args().size() == 1) {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.tokensManager.sendInfoMessage(c.sender(), target, false);
                    }
                })
                .registerAndBind(this, "blocks");
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, Text.colorize(this.getConfig().getString("messages." + key)));
        }
    }

    public static String getMessage(String key) {
        return messages.get(key);
    }
}
