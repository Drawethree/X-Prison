package me.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import me.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import me.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import me.drawethree.ultraprisoncore.gangs.enums.GangCreateResult;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangCreateCommand extends GangCommand {

    @Override
    public String getUsage() {
        return ChatColor.RED + "/gang create [gang]";
    }

    public GangCreateCommand(UltraPrisonGangs plugin) {
        super(plugin, "create", "new");
    }

    @Override
    public boolean execute(CommandSender sender, ImmutableList<String> args) {
        if (sender instanceof Player && args.size() == 1) {
			return this.plugin.getGangsManager().createGang(args.get(0), (Player) sender) == GangCreateResult.SUCCESS;
        }
        return false;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return true;
    }
}
