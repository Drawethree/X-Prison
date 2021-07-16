package me.drawethree.ultraprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public abstract class TokensCommand {

    public static final HashMap<String, TokensCommand> commands;

    static {
        commands = new HashMap<>();
        commands.put("give", new TokensGiveCommand(UltraPrisonTokens.getInstance()));
        commands.put("pay", new TokensPayCommand(UltraPrisonTokens.getInstance()));
        commands.put("remove", new TokensRemoveCommand(UltraPrisonTokens.getInstance()));
        commands.put("set", new TokensSetCommand(UltraPrisonTokens.getInstance()));
        commands.put("withdraw", new TokensWithdrawCommand(UltraPrisonTokens.getInstance()));
        commands.put("help", new TokensHelpCommand(UltraPrisonTokens.getInstance()));
    }

    protected UltraPrisonTokens plugin;

    public TokensCommand(UltraPrisonTokens plugin) {

        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public static TokensCommand getCommand(String arg) {
        return commands.get(arg.toLowerCase());
    }
}
