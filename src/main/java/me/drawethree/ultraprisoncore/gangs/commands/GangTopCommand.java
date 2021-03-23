package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GangTopCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang top";
    }

    public GangTopCommand(UltraPrisonGangs plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 0) {
            return this.plugin.getGangsManager().sendGangTop(sender);
        }
        return false;
    }


    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
