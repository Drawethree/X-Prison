package me.drawethree.ultraprisoncore.gangs.commands;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.models.Gang;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class GangRemoveCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang remove <player>";
    }

    public GangRemoveCommand(UltraPrisonGangs plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (args.size() == 1 && sender instanceof Player) {
            Player p = (Player) sender;
            Optional<Gang> gang = this.plugin.getGangsManager().getPlayerGang(p);
            OfflinePlayer target = Players.getOfflineNullable(args.get(0));
            return this.plugin.getGangsManager().removeFromGang(p,gang,target);
        }
        return false;
    }


    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }

}
