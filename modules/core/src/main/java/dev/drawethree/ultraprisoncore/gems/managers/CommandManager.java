package dev.drawethree.ultraprisoncore.gems.managers;

import dev.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import dev.drawethree.ultraprisoncore.gems.commands.*;
import dev.drawethree.ultraprisoncore.utils.player.PlayerUtils;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static dev.drawethree.ultraprisoncore.gems.UltraPrisonGems.GEMS_ADMIN_PERM;


public class CommandManager {

    @Getter
    private final UltraPrisonGems plugin;
    private final Set<GemsCommand> commands;
    private CooldownMap<CommandSender> gemsCommandCooldownMap;
    private String[] gemsCommandAliases;
    private String[] gemsTopCommandAliases;
    private String[] gemsMessageCommandAliases;

    public CommandManager(UltraPrisonGems plugin) {
        this.plugin = plugin;
        this.commands = new HashSet<>();
        this.gemsCommandCooldownMap = CooldownMap.create(Cooldown.of(plugin.getCommandCooldown(), TimeUnit.SECONDS));
    }

    private boolean checkCommandCooldown(CommandSender sender) {
        if (sender.hasPermission(GEMS_ADMIN_PERM)) {
            return true;
        }
        if (!gemsCommandCooldownMap.test(sender)) {
            PlayerUtils.sendMessage(sender, this.plugin.getMessage("cooldown").replace("%time%", String.format("%,d", this.gemsCommandCooldownMap.remainingTime(sender, TimeUnit.SECONDS))));
            return false;
        }
        return true;
    }

    private void registerCommands() {

        this.commands.clear();

        this.registerCommand(new GemsGiveCommand(this));
        this.registerCommand(new GemsPayCommand(this));
        this.registerCommand(new GemsRemoveCommand(this));
        this.registerCommand(new GemsSetCommand(this));
        this.registerCommand(new GemsWithdrawCommand(this));
        this.registerCommand(new GemsHelpCommand(this));

        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0 && c.sender() instanceof Player) {
                        this.plugin.getGemsManager().sendInfoMessage(c.sender(), (OfflinePlayer) c.sender());
                        return;
                    }

                    GemsCommand subCommand = this.getCommand(c.rawArg(0));
                    if (subCommand != null) {
                        if (subCommand.canExecute(c.sender())) {
                            subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                        } else {
                            PlayerUtils.sendMessage(c.sender(), this.plugin.getMessage("no_permission"));
                        }
                    } else {
                        if (!checkCommandCooldown(c.sender())) {
                            return;
                        }
                        OfflinePlayer target = Players.getOfflineNullable(c.rawArg(0));
                        this.plugin.getGemsManager().sendInfoMessage(c.sender(), target);
                    }
                })
                .registerAndBind(this.plugin.getCore(), this.gemsCommandAliases);
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.plugin.getGemsManager().sendGemsTop(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), this.gemsTopCommandAliases);

        // /gemsmessage
        Commands.create()
                .assertPlayer()
                .handler(c -> this.plugin.getGemsManager().toggleGemsMessage(c.sender())).registerAndBind(this.plugin.getCore(), this.gemsMessageCommandAliases);

    }

    private void registerCommand(GemsCommand command) {
        this.commands.add(command);
    }

    private GemsCommand getCommand(String arg) {
        for (GemsCommand command : this.commands) {

            if (command.getName().equalsIgnoreCase(arg)) {
                return command;
            }

            if (command.getAliases() == null) {
                continue;
            }

            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(arg)) {
                    return command;
                }
            }
        }
        return null;
    }

    private void loadVariables() {
        this.gemsCommandAliases = this.plugin.getConfig().get().getStringList("gems-command-aliases").toArray(new String[0]);
        this.gemsTopCommandAliases = this.plugin.getConfig().get().getStringList("gems-top-command-aliases").toArray(new String[0]);
        this.gemsMessageCommandAliases = this.plugin.getConfig().get().getStringList("gems-message-command-aliases").toArray(new String[0]);
    }

    public Set<GemsCommand> getAll() {
        return new HashSet<>(this.commands);
    }

    public void reload() {
        Map<CommandSender, Cooldown> cooldownMap = this.gemsCommandCooldownMap.getAll();
        this.gemsCommandCooldownMap = CooldownMap.create(Cooldown.of(plugin.getCommandCooldown(), TimeUnit.SECONDS));
        cooldownMap.forEach((commandSender, cooldown) -> this.gemsCommandCooldownMap.put(commandSender, cooldown));
        this.loadVariables();
    }

    public void enable() {
        this.loadVariables();
        this.registerCommands();
    }
}
