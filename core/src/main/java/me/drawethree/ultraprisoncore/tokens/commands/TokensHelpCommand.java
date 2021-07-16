package me.drawethree.ultraprisoncore.tokens.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.tokens.UltraPrisonTokens;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

public class TokensHelpCommand extends TokensCommand {

    public TokensHelpCommand(UltraPrisonTokens plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e&lTOKEN HELP MENU "));
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e/tokens pay [player] [amount]"));
            sender.sendMessage(Text.colorize("&e/tokens withdraw [amount] [value]"));
            sender.sendMessage(Text.colorize("&e/tokens [player]"));
            if (sender.hasPermission(UltraPrisonTokens.TOKENS_ADMIN_PERM)) {
                sender.sendMessage(Text.colorize("&e/tokens give [player] [amount]"));
                sender.sendMessage(Text.colorize("&e/tokens remove [player] [amount]"));
                sender.sendMessage(Text.colorize("&e/tokens set [player] [amount]"));
            }
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            return true;
        }
        return false;
    }
}
