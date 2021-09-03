package me.drawethree.ultraprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import org.bukkit.command.CommandSender;

public abstract class TokensCommand {

    protected UltraPrisonTokens plugin;

    TokensCommand(UltraPrisonTokens plugin) {

        this.plugin = plugin;
    }

    public abstract boolean execute(CommandSender sender, ImmutableList<String> args);

    public abstract boolean canExecute(CommandSender sender);

}
