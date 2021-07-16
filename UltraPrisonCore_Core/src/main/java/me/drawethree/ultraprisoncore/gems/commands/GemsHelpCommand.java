package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

public class GemsHelpCommand extends GemsCommand {

    public GemsHelpCommand(UltraPrisonGems plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e&lGEMS HELP MENU "));
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e/gems [player]"));
            sender.sendMessage(Text.colorize("&e/gems pay [player] [amount]"));
            if (sender.hasPermission(UltraPrisonGems.GEMS_ADMIN_PERM)) {
                sender.sendMessage(Text.colorize("&e/gems give [player] [amount]"));
                sender.sendMessage(Text.colorize("&e/gems remove [player] [amount]"));
                sender.sendMessage(Text.colorize("&e/gems set [player] [amount]"));
            }
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            return true;
        }
        return false;
    }
}
