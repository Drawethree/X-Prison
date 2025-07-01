package dev.drawethree.xprison.blocks.managers;

import dev.drawethree.xprison.blocks.XPrisonBlocks;
import dev.drawethree.xprison.tokens.utils.TokensConstants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommandManager {

    @Getter
    private final XPrisonBlocks plugin;
    private CooldownMap<CommandSender> blocksCommandCooldownMap;

    public CommandManager(XPrisonBlocks plugin) {
        this.plugin = plugin;
        this.blocksCommandCooldownMap = CooldownMap.create(Cooldown.of(plugin.getBlocksConfig().getCommandCooldown(), TimeUnit.SECONDS));
    }


    private boolean checkCommandCooldown(CommandSender sender) {
        if (sender.hasPermission(TokensConstants.TOKENS_ADMIN_PERM)) {
            return true;
        }
        if (!blocksCommandCooldownMap.test(sender)) {
            PlayerUtils.sendMessage(sender, this.plugin.getBlocksConfig().getMessage("cooldown").replace("%time%", String.format("%,d", this.blocksCommandCooldownMap.remainingTime(sender, TimeUnit.SECONDS))));
            return false;
        }
        return true;
    }

    private void registerCommands() {
        // /blocks
        Commands.create()
                .handler(c -> {
                    if (!checkCommandCooldown(c.sender())) {
                        return;
                    }

                    if (c.args().isEmpty()) {
                        this.plugin.getBlocksManager().sendInfoMessage(c.sender(), (OfflinePlayer) c.sender());
                    } else if (c.args().size() == 1) {
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.plugin.getBlocksManager().sendInfoMessage(c.sender(), target);
                    }
                })
                .registerAndBind(this.plugin.getCore(), "blocks");

        // /blocksadmin, /blocksa
        Commands.create()
                .tabHandler(c -> Arrays.asList("add", "remove", "set"))
                .assertPermission(TokensConstants.TOKENS_ADMIN_PERM, this.plugin.getBlocksConfig().getMessage("no_permission"))
                .handler(c -> {
                    if (c.args().size() == 3) {

                        OfflinePlayer target = c.arg(1).parseOrFail(OfflinePlayer.class);
                        long amount = c.arg(2).parseOrFail(Long.class);

                        switch (c.rawArg(0).toLowerCase()) {
                            case "add":
                                this.plugin.getBlocksManager().addBlocksBroken(c.sender(), target, amount);
                                break;
                            case "remove":
                                this.plugin.getBlocksManager().removeBlocksBroken(c.sender(), target, amount);
                                break;
                            case "set":
                                this.plugin.getBlocksManager().setBlocksBroken(c.sender(), target, amount);
                                break;
                            default:
                                PlayerUtils.sendMessage(c.sender(), "&c/blocksadmin <add/set/remove> <player> <amount>");
                                break;
                        }
                    } else {
                        PlayerUtils.sendMessage(c.sender(), "&c/blocksadmin <add/set/remove> <player> <amount>");
                    }
                })
                .registerAndBind(this.plugin.getCore(), "blocksadmin", "blocksa");

        // /blockstop, / blocktop
        Commands.create()
                .handler(c -> {
                    if (c.args().isEmpty()) {
                        this.plugin.getBlocksManager().sendBlocksTop(c.sender());
                    }
                })
                .registerAndBind(this.plugin.getCore(), this.plugin.getBlocksConfig().getBlocksTopCommandAliases());

        // /blockstopweekly, /blockstopw
        Commands.create()
                .handler(c -> {
                    if (c.args().isEmpty()) {
                        this.plugin.getBlocksManager().sendBlocksTopWeekly(c.sender());
                    }
                })
                .registerAndBind(this.plugin.getCore(), "blockstopweekly", "blockstopw");

        // /blockstopweeklyreset
        Commands.create()
                .assertPermission(TokensConstants.TOKENS_ADMIN_PERM, this.plugin.getBlocksConfig().getMessage("no_permission"))
                .handler(c -> {
                    if (c.args().isEmpty()) {
                        this.plugin.getBlocksManager().resetBlocksTopWeekly(c.sender());
                    }
                })
                .registerAndBind(this.plugin.getCore(), "blockstopweeklyreset");
    }

    public void reload() {
        Map<CommandSender, Cooldown> cooldownMap = this.blocksCommandCooldownMap.getAll();
        this.blocksCommandCooldownMap = CooldownMap.create(Cooldown.of(plugin.getBlocksConfig().getCommandCooldown(), TimeUnit.SECONDS));
        cooldownMap.forEach((commandSender, cooldown) -> this.blocksCommandCooldownMap.put(commandSender, cooldown));
    }

    public void enable() {
        this.registerCommands();
    }
}
