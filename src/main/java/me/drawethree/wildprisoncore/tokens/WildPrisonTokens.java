package me.drawethree.wildprisoncore.tokens;


import lombok.Getter;
import me.drawethree.wildprisoncore.WildPrisonCore;
import me.drawethree.wildprisoncore.config.FileManager;
import me.drawethree.wildprisoncore.tokens.api.WildPrisonTokensAPI;
import me.drawethree.wildprisoncore.tokens.api.WildPrisonTokensAPIImpl;
import me.drawethree.wildprisoncore.tokens.commands.TokensCommand;
import me.drawethree.wildprisoncore.tokens.managers.TokensManager;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class WildPrisonTokens {

    public static final String TOKENS_ADMIN_PERM = "wildprison.tokens.admin";

    @Getter
    private static WildPrisonTokens instance;

    @Getter
    private FileManager.Config config;

    @Getter
    private FileManager.Config blockRewardsConfig;

    @Getter
    private WildPrisonTokensAPI api;

    @Getter
    private TokensManager tokensManager;
    @Getter
    private WildPrisonCore core;

    private HashMap<String, String> messages;
    private double chance;
    private long minAmount;
    private long maxAmount;

    public WildPrisonTokens(WildPrisonCore wildPrisonCore) {
        instance = this;
        this.core = wildPrisonCore;
        this.config = wildPrisonCore.getFileManager().getConfig("tokens.yml").copyDefaults(true).save();
        this.blockRewardsConfig = wildPrisonCore.getFileManager().getConfig("block-rewards.yml").copyDefaults(true).save();
        this.loadMessages();
        this.loadVariables();
        this.tokensManager = new TokensManager(this);
        this.api = new WildPrisonTokensAPIImpl(this.tokensManager);
    }

    private void loadVariables() {
        this.chance = getConfig().get().getDouble("tokens.breaking.chance");
        this.minAmount = getConfig().get().getLong("tokens.breaking.min");
        this.maxAmount = getConfig().get().getLong("tokens.breaking.max");
    }


    public void enable() {
        this.registerCommands();
        this.registerEvents();
    }


    public void disable() {
        this.tokensManager.stopUpdating();
        this.tokensManager.saveWeeklyReset();
        this.tokensManager.savePlayerDataOnDisable();
    }

    private void registerEvents() {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> e.getItem() != null && e.getItem().getType() == Material.DOUBLE_PLANT && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
                .handler(e -> {
                    if (e.getItem().hasItemMeta()) {
                        this.tokensManager.redeemTokens(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking());
                    }
                })
                .bindWith(core);
        Events.subscribe(BlockBreakEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> this.core.getWorldGuard().getRegionManager(e.getBlock().getWorld()).getApplicableRegions(e.getBlock().getLocation()).getRegions().stream().filter(region -> region.getId().toLowerCase().startsWith("mine")).findAny().isPresent())
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE && !e.getPlayer().getWorld().getName().equalsIgnoreCase("pvp") && !e.getPlayer().getWorld().getName().equalsIgnoreCase("plots"))
                .handler(e -> {
                    tokensManager.addBlocksBroken(e.getPlayer(), 1);
                    if (chance >= ThreadLocalRandom.current().nextDouble()) {
                        long randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
                        tokensManager.giveTokens(e.getPlayer(), randAmount, null);
                    }
                }).bindWith(core);
    }

    private void registerCommands() {
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.tokensManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender(), true);
                        return;
                    }
                    TokensCommand subCommand = TokensCommand.getCommand(c.rawArg(0));
                    if (subCommand != null) {
                        subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                    } else {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.tokensManager.sendInfoMessage(c.sender(), target, true);
                    }
                })
                .registerAndBind(core, "tokens", "token");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendBlocksTop(c.sender());
                    }
                })
                .registerAndBind(core, "blockstop", "blocktop");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendBlocksTopWeekly(c.sender());
                    }
                })
                .registerAndBind(core, "blockstopweekly", "blockstopw");
        Commands.create()
                .assertOp()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.resetBlocksTopWeekly(c.sender());
                    }
                })
                .registerAndBind(core, "blockstopweeklyreset");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendTokensTop(c.sender());
                    }
                })
                .registerAndBind(core, "tokenstop", "tokentop");
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.tokensManager.sendInfoMessage(c.sender(), (OfflinePlayer) c.sender(), false);
                    } else if (c.args().size() == 1) {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.tokensManager.sendInfoMessage(c.sender(), target, false);
                    }
                })
                .registerAndBind(core, "blocks", "block");
    }

    private void loadMessages() {
        messages = new HashMap<>();
        for (String key : this.getConfig().get().getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, Text.colorize(this.getConfig().get().getString("messages." + key)));
        }
    }

    public String getMessage(String key) {
        return messages.get(key);
    }
}
