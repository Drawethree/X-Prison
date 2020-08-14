package me.drawethree.wildprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.wildprisoncore.gems.WildPrisonGems;
import me.drawethree.wildprisoncore.tokens.WildPrisonTokens;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

public class GemsHelpCommand extends GemsCommand {

    public GemsHelpCommand(WildPrisonGems plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e&lGEMS HELP MENU "));
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e/gems [player]"));
            if (sender.hasPermission(WildPrisonGems.GEMS_ADMIN_PERM)) {
                sender.sendMessage(Text.colorize("&e/gems give [amount] [player]"));
                sender.sendMessage(Text.colorize("&e/gems remove [amount] [player]"));
                sender.sendMessage(Text.colorize("&e/gems set [amount] [player]"));
            }
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            return true;
        }
        return false;
    }
}
