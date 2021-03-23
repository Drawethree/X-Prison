package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.lucko.helper.text.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GangHelpCommand extends GangCommand {


    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang help";
    }

    public GangHelpCommand(UltraPrisonGangs plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e&lGANG HELP MENU "));
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            sender.sendMessage(Text.colorize("&e/gang create [gang]"));
            sender.sendMessage(Text.colorize("&e/gang invite [player]"));
            sender.sendMessage(Text.colorize("&e/gang info [player]"));
            sender.sendMessage(Text.colorize("&e/gang accept"));
            sender.sendMessage(Text.colorize("&e/gang leave"));
            sender.sendMessage(Text.colorize("&e/gang disband"));
            if (sender.hasPermission(UltraPrisonGangs.GANGS_ADMIN_PERM)) {
            }
            sender.sendMessage(Text.colorize("&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------"));
            return true;
        }
        return false;
    }
}
