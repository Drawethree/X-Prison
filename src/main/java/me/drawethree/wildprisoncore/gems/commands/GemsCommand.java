package me.drawethree.wildprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.gems.WildPrisonGems;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class GemsCommand {

    public static final HashMap<String, GemsCommand> commands;

    static {
        commands = new HashMap<>();
        commands.put("give", new GemsGiveCommand(WildPrisonGems.getInstance()));
        commands.put("remove", new GemsRemoveCommand(WildPrisonGems.getInstance()));
        commands.put("set", new GemsSetCommand(WildPrisonGems.getInstance()));
        commands.put("help", new GemsHelpCommand(WildPrisonGems.getInstance()));
    }

    protected WildPrisonGems plugin;

    public GemsCommand(WildPrisonGems plugin) {

        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static GemsCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}
