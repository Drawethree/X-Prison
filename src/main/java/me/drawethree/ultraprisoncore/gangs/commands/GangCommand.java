package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.drawethree.ultraprisoncore.tokens.commands.*;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class GangCommand {

    public static final HashMap<String, GangCommand> commands;

    static {
        commands = new HashMap<>();
        commands.put("help", new GangHelpCommand(UltraPrisonGangs.getInstance()));
        commands.put("info", new GangInfoCommand(UltraPrisonGangs.getInstance()));
        commands.put("create", new GangCreateCommand(UltraPrisonGangs.getInstance()));
        commands.put("invite", new GangInviteCommand(UltraPrisonGangs.getInstance()));
        commands.put("accept", new GangAcceptCommand(UltraPrisonGangs.getInstance()));
        commands.put("leave", new GangLeaveCommand(UltraPrisonGangs.getInstance()));
        commands.put("disband", new GangDisbandCommand(UltraPrisonGangs.getInstance()));
        commands.put("remove", new GangRemoveCommand(UltraPrisonGangs.getInstance()));
        commands.put("top", new GangTopCommand(UltraPrisonGangs.getInstance()));
        commands.put("admin", new GangAdminCommand(UltraPrisonGangs.getInstance()));
        commands.put("value", new GangValueCommand(UltraPrisonGangs.getInstance()));
        commands.put("chat", new GangChatCommand(UltraPrisonGangs.getInstance()));
    }

    protected UltraPrisonGangs plugin;

    public abstract String getUsage();

    public GangCommand(UltraPrisonGangs plugin) {
        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static GangCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }

    public abstract boolean canExecute(CommandSender sender);
}
