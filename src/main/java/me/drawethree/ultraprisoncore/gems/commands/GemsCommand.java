package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class GemsCommand {

    public static final HashMap<String, GemsCommand> commands;

    static {
        commands = new HashMap<>();
        commands.put("give", new GemsGiveCommand(UltraPrisonGems.getInstance()));
        commands.put("remove", new GemsRemoveCommand(UltraPrisonGems.getInstance()));
        commands.put("set", new GemsSetCommand(UltraPrisonGems.getInstance()));
        commands.put("help", new GemsHelpCommand(UltraPrisonGems.getInstance()));
        commands.put("pay", new GemsPayCommand(UltraPrisonGems.getInstance()));
    }

    protected UltraPrisonGems plugin;

    public GemsCommand(UltraPrisonGems plugin) {

        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static GemsCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}
