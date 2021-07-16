package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GangAdminCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang admin <add/remove/disband> <player> <gang>";
    }

    public GangAdminCommand(UltraPrisonGangs plugin) {
        super(plugin, "admin");
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() > 0) {
            String operation = args.get(0);
            if (operation.equalsIgnoreCase("add")) {
                Player target = Players.getNullable(args.get(1));
                Optional<Gang> gangOptional = this.plugin.getGangsManager().getGangWithName(args.get(2));
                return this.plugin.getGangsManager().forceAdd(sender, target, gangOptional);
            } else if (operation.equalsIgnoreCase("remove")) {
                Player target = Players.getNullable(args.get(1));
                return this.plugin.getGangsManager().forceRemove(sender, target);
            } else if (operation.equalsIgnoreCase("disband")) {
                Optional<Gang> gangOptional = this.plugin.getGangsManager().getGangWithName(args.get(1));
                return this.plugin.getGangsManager().forceDisband(sender, gangOptional);
            }
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return sender.hasPermission(UltraPrisonGangs.GANGS_ADMIN_PERM);
    }
}
