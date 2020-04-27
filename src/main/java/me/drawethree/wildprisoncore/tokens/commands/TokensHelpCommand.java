package me.drawethree.wildprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import org.bukkit.command.CommandSender;

public class TokensHelpCommand extends TokensCommand {

    public TokensHelpCommand(WildPrisonTokens plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("§e§lTokens Command Menu:");
            sender.sendMessage("§6§l* §e/tokens pay [amount] [player]");
            sender.sendMessage("§6§l* §e/tokens withdraw [amount] [value]");
            sender.sendMessage("§6§l* §e/tokens [player]");
            if (sender.hasPermission(WildPrisonTokens.TOKENS_ADMIN_PERM)) {
                sender.sendMessage("§e§lTokens Admin Commands Menu:");
                sender.sendMessage("§6§l* §e/tokens give [amount] [player]");
                sender.sendMessage("§6§l* §e/tokens remove [amount] [player]");
                sender.sendMessage("§6§l* §e/tokens set [amount] [player]");
            }
            return true;
        }
        return false;
    }
}
