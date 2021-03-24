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
        super(plugin, "help", "?");
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.isEmpty()) {
            this.plugin.getGangsManager().sendHelpMenu(sender);
            if (sender.hasPermission(UltraPrisonGangs.GANGS_ADMIN_PERM)) {
                this.plugin.getGangsManager().sendAdminHelpMenu(sender);
            }
            return true;
        }
        return false;
    }


    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
