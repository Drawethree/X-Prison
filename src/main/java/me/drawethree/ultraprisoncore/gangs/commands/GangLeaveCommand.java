package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangLeaveCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang leave";
    }

    public GangLeaveCommand(UltraPrisonGangs plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 0 && sender instanceof Player) {
            Player p = (Player) sender;
            return this.plugin.getGangsManager().leaveGang(p);
        }
        return false;
    }
}
