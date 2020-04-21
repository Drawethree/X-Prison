package me.drawethree.wildprisontokens.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class TokensCommand {

    public static final HashMap<String, TokensCommand> commands;

    static {
        commands = new HashMap<>();

        commands.put("give", new TokensGiveCommand());
        commands.put("pay", new TokensPayCommand());
        commands.put("remove", new TokensRemoveCommand());
        commands.put("set", new TokensSetCommand());
        commands.put("withdraw", new TokensWithdrawCommand());
        commands.put("help", new TokensHelpCommand());
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static TokensCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}
