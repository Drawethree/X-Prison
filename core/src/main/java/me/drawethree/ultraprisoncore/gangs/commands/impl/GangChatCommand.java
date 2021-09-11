package me.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GangChatCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang chat";
    }

    public GangChatCommand(UltraPrisonGangs plugin) {
        super(plugin, "chat", "c");
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 0 && sender instanceof Player) {
            Player p = (Player) sender;
            return this.plugin.getGangsManager().toggleGangChat(p);
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
