package me.drawethree.wildprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class TokensCommand {

    public static final HashMap<String, TokensCommand> commands;

    static {
        commands = new HashMap<>();
        commands.put("give", new TokensGiveCommand(WildPrisonTokens.getInstance()));
        commands.put("pay", new TokensPayCommand(WildPrisonTokens.getInstance()));
        commands.put("remove", new TokensRemoveCommand(WildPrisonTokens.getInstance()));
        commands.put("set", new TokensSetCommand(WildPrisonTokens.getInstance()));
        commands.put("withdraw", new TokensWithdrawCommand(WildPrisonTokens.getInstance()));
        commands.put("help", new TokensHelpCommand(WildPrisonTokens.getInstance()));
    }

    protected WildPrisonTokens plugin;

    public TokensCommand(WildPrisonTokens plugin) {

        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static TokensCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}
