package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class GangCommand {

    public static final HashMap<String, GangCommand> commands;

    static {
        commands = new HashMap<>();
        registerCommand(new GangHelpCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangHelpCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangInfoCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangCreateCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangInviteCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangAcceptCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangLeaveCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangDisbandCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangKickCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangTopCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangAdminCommand(UltraPrisonGangs.getInstance()));
        registerCommand(new GangValueCommand(UltraPrisonGangs.getInstance()));
        //registerCommand(new GangChatCommand(UltraPrisonGangs.getInstance()));
    }


    private static void registerCommand(GangCommand command) {
        commands.put(command.name, command);

        if (command.aliases == null || command.aliases.length == 0) {
            return;
        }

        for (String alias : command.aliases) {
            commands.put(alias,command);
        }
    }

    protected UltraPrisonGangs plugin;
    protected String name;
    protected String[] aliases;

    public abstract String getUsage();

    public GangCommand(UltraPrisonGangs plugin, String name, String... aliases) {
        this.plugin = plugin;
        this.name = name;
        this.aliases = aliases;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static GangCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }

    public abstract boolean canExecute(CommandSender sender);
}
