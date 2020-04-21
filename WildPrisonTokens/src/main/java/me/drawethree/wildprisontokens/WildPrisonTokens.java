package me.drawethree.wildprisontokens;


import lombok.Getter;
import me.drawethree.wildprisontokens.api.WildPrisonTokensAPI;
import me.drawethree.wildprisontokens.api.WildPrisonTokensAPIImpl;
import me.drawethree.wildprisontokens.commands.TokensCommand;
import me.drawethree.wildprisontokens.database.MySQLDatabase;
import me.drawethree.wildprisontokens.managers.TokensManager;
import me.drawethree.wildprisontokens.placeholders.TokensPlaceholder;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

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

    @Override
    protected void load() {
        instance = this;
        saveDefaultConfig();
        this.loadMessages();
        api = new WildPrisonTokensAPIImpl(this.tokensManager);
    }

    @Override
    protected void enable() {
        this.tokensManager = new TokensManager(this);
        this.sqlDatabase = new MySQLDatabase(this);
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
    }

    private void registerPlaceholders() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new TokensPlaceholder(this).register();
        }
    }

    private void registerCommands() {
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.tokensManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender());
                        return;
                    }
                    TokensCommand subCommand = TokensCommand.getCommand(c.rawArg(0));
                    if (subCommand != null) {
                        subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                    } else {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.tokensManager.sendInfoMessage(c.sender(), target);
                    }
                })
                .registerAndBind(this, "tokens");
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
