package me.drawethree.ultraprisoncore.tokens;


import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.api.enums.ReceiveCause;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import me.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPI;
import me.drawethree.ultraprisoncore.tokens.api.UltraPrisonTokensAPIImpl;
import me.drawethree.ultraprisoncore.tokens.commands.TokensCommand;
import me.drawethree.ultraprisoncore.tokens.managers.TokensManager;
import me.drawethree.ultraprisoncore.utils.compat.CompMaterial;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class UltraPrisonTokens implements UltraPrisonModule {

    public static final String TOKENS_ADMIN_PERM = "ultraprison.tokens.admin";

    @Getter
    private static UltraPrisonTokens instance;

    @Getter
    private FileManager.Config config;

    @Getter
    private FileManager.Config blockRewardsConfig;

    @Getter
    private UltraPrisonTokensAPI api;

    @Getter
    private TokensManager tokensManager;
    @Getter
    private UltraPrisonCore core;

    private HashMap<String, String> messages;
    private double chance;
    private long minAmount;
    private long maxAmount;
    private boolean enabled;

    public UltraPrisonTokens(UltraPrisonCore prisonCore) {
        instance = this;
        this.core = prisonCore;
        this.config = prisonCore.getFileManager().getConfig("tokens.yml").copyDefaults(true).save();
        this.blockRewardsConfig = prisonCore.getFileManager().getConfig("block-rewards.yml").copyDefaults(true).save();
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void reload() {
        this.config = this.core.getFileManager().getConfig("tokens.yml");
        this.blockRewardsConfig = this.core.getFileManager().getConfig("block-rewards.yml");

        this.config.reload();
        this.blockRewardsConfig.reload();
        this.loadMessages();
        this.loadVariables();
        this.tokensManager.reloadConfig();
    }

    private void loadVariables() {
        this.chance = getConfig().get().getDouble("tokens.breaking.chance");
        this.minAmount = getConfig().get().getLong("tokens.breaking.min");
        this.maxAmount = getConfig().get().getLong("tokens.breaking.max");
    }


    @Override
    public void enable() {
        this.enabled = true;
        this.loadMessages();
        this.loadVariables();
        this.tokensManager = new TokensManager(this);
        this.api = new UltraPrisonTokensAPIImpl(this.tokensManager);
        this.registerCommands();
        this.registerEvents();
    }


    @Override
    public void disable() {
        this.tokensManager.stopUpdating();
        this.tokensManager.saveWeeklyReset();
        this.tokensManager.savePlayerDataOnDisable();
        this.enabled = false;

    }

    @Override
    public String getName() {
        return "Tokens";
    }

    private void registerEvents() {

        Events.subscribe(PlayerInteractEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getItem() != null && e.getItem().getType() == CompMaterial.SUNFLOWER.toMaterial() && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
                .handler(e -> {
                    if (e.getItem().hasItemMeta()) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        this.tokensManager.redeemTokens(e.getPlayer(), e.getItem(), e.getPlayer().isSneaking());
                    }
                })
                .bindWith(core);

        Events.subscribe(BlockBreakEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().filter(region -> region.getId().toLowerCase().startsWith("mine")).findAny().isPresent())
                .filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == CompMaterial.DIAMOND_PICKAXE.toMaterial())
                .handler(e -> {
					tokensManager.addBlocksBroken(null, e.getPlayer(), 1);
                    if (chance >= ThreadLocalRandom.current().nextDouble()) {
                        boolean luckyBooster = LuckyBoosterEnchant.hasLuckyBoosterRunning(e.getPlayer());

                        long randAmount = ThreadLocalRandom.current().nextLong(minAmount, maxAmount);
                        randAmount = luckyBooster ? randAmount * 2 : randAmount;

                        tokensManager.giveTokens(e.getPlayer(), randAmount, null, ReceiveCause.MINING);
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
                }).registerAndBind(core, "tokens", "token");

        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    this.tokensManager.toggleTokenMessage(c.sender());
                }).registerAndBind(core, "tokenmessage");

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
                .registerAndBind(core, "blockstopweekly", "blockstopw", "btw");
        Commands.create()
                .assertPermission("ultraprison.tokens.admin")
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
		Commands.create()
                .assertPermission("ultraprison.tokens.admin")
				.handler(c -> {
					if (c.args().size() == 3) {

						Player target = c.arg(1).parseOrFail(Player.class);
						long amount = c.arg(2).parseOrFail(Long.class);

						switch (c.rawArg(0).toLowerCase()) {
							case "add":
								this.tokensManager.addBlocksBroken(c.sender(), target, amount);
								break;
							case "remove":
								this.tokensManager.removeBlocksBroken(c.sender(), target, amount);
								break;
							case "set":
								this.tokensManager.setBlocksBroken(c.sender(), target, amount);
								break;
							default:
								c.sender().sendMessage(Text.colorize("&c/blocksadmin <add/set/remove> <player> <amount>"));
								break;
						}
					} else {
						c.sender().sendMessage(Text.colorize("&c/blocksadmin <add/set/remove> <player> <amount>"));
					}
				})
				.registerAndBind(core, "blocksadmin", "blocksa");
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
