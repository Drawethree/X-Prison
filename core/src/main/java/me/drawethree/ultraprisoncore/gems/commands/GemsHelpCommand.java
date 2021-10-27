package me.drawethree.ultraprisoncore.gems.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gems.UltraPrisonGems;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.lucko.helper.text.Text;
import org.bukkit.command.CommandSender;

public class GemsHelpCommand extends GemsCommand {

    public GemsHelpCommand(UltraPrisonGems plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&lGEMS HELP MENU "));
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/gems [player]"));
			PlayerUtils.sendMessage(sender, Text.colorize("&e/gems pay [player] [amount]"));
            PlayerUtils.sendMessage(sender, Text.colorize("&e/gems withdraw [amount] [value]"));
            if (sender.hasPermission(UltraPrisonGems.GEMS_ADMIN_PERM)) {
				PlayerUtils.sendMessage(sender, Text.colorize("&e/gems give [player] [amount]"));
				PlayerUtils.sendMessage(sender, Text.colorize("&e/gems remove [player] [amount]"));
				PlayerUtils.sendMessage(sender, Text.colorize("&e/gems set [player] [amount]"));
            }
			PlayerUtils.sendMessage(sender, Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            return true;
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
