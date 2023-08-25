package dev.drawethree.xprison.gems.commands;

import com.google.common.collect.ImmutableList;
import dev.drawethree.xprison.gems.managers.CommandManager;
import dev.drawethree.xprison.interfaces.Permissionable;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class GemsCommand implements Permissionable {

    protected static final String PERMISSION_ROOT = "xprison.gems.command.";

    @Getter
    private final String name;
    protected final CommandManager commandManager;
    @Getter
    private final String[] aliases;

    GemsCommand(CommandManager commandManager, String name, String... aliases) {
        this.commandManager = commandManager;
        this.name = name;
        this.aliases = aliases;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public abstract boolean canExecute(CommandSender sender);

    public abstract String getUsage();

    @Override
    public String getRequiredPermission() {
        return PERMISSION_ROOT + this.name;
    }

    public abstract List<String> getTabComplete(List<String> args);
}
