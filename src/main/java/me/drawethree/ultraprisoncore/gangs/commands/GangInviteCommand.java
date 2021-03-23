package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangInviteCommand extends GangCommand{

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang invite [player]";
    }

    public GangInviteCommand(UltraPrisonGangs plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (sender instanceof Player && args.size() == 1) {
            Player p = (Player) sender;
            Player target = Players.getNullable(args.get(0));
            return this.plugin.getGangsManager().invitePlayer(p, target);
        }
        return false;
    }


    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
