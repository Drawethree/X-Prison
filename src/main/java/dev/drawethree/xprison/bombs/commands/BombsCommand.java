package dev.drawethree.xprison.bombs.commands;

import dev.drawethree.xprison.bombs.XPrisonBombs;
import dev.drawethree.xprison.bombs.commands.subcommand.BombsSubCommand;
import dev.drawethree.xprison.bombs.commands.subcommand.impl.GiveSubCommand;
import lombok.Getter;
import me.lucko.helper.Commands;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class BombsCommand {

    private static final String PERMISSION_ROOT = "xprison.bombs.admin";

    @Getter
    private final XPrisonBombs plugin;
    private final Map<String, BombsSubCommand> subCommands;

    public BombsCommand(XPrisonBombs plugin) {
        this.plugin = plugin;
        this.subCommands = new LinkedHashMap<>();
    }

    public void register() {
        this.registerSubCommands();
        this.registerMainCommand();
    }

    private void registerSubCommands() {
        registerSubCommand(new GiveSubCommand(this));
    }

    private void registerMainCommand() {
        Commands.create()
                .tabHandler(this::createTabHandler)
                .assertPermission(PERMISSION_ROOT, this.plugin.getConfig().getMessage("no-permission"))
                .handler(c -> {

                    if (c.args().isEmpty()) {
                        sendHelpMenu(c.sender());
                        return;
                    }

                    BombsSubCommand subCommand = this.getSubCommand(Objects.requireNonNull(c.rawArg(0)));

                    if (subCommand != null) {
                        subCommand.execute(c.sender(), c.args().subList(1, c.args().size()));
                    } else {
                        sendHelpMenu(c.sender());
                    }
                }).registerAndBind(this.plugin, this.plugin.getConfig().getBombsCommandAliases());
    }

    private List<String> createTabHandler(CommandContext<CommandSender> context) {

        if (context.args().isEmpty()) {
            return new ArrayList<>(this.subCommands.keySet());
        }

        BombsSubCommand subCommand = getSubCommand(context.rawArg(0));

        if (subCommand != null) {
            return subCommand.getTabComplete();
        }

        return new ArrayList<>(this.subCommands.keySet());
    }

    private BombsSubCommand getSubCommand(String name) {
        return this.subCommands.get(name.toLowerCase());
    }

    private void registerSubCommand(BombsSubCommand subCommand) {
        for (String alias : subCommand.getAliases()) {
            this.subCommands.put(alias.toLowerCase(), subCommand);
        }
    }

    public void sendHelpMenu(CommandSender sender) {
        for (BombsSubCommand subCommand : this.subCommands.values()) {
            sender.sendMessage(subCommand.getUsage());
        }
    }
}
