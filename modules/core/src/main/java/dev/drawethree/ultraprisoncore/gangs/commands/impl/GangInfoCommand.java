package dev.drawethree.ultraprisoncore.gangs.commands.impl;

import com.google.common.collect.ImmutableList;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GangInfoCommand extends GangCommand {

	public GangInfoCommand(UltraPrisonGangs plugin) {
		super(plugin, "info", "inspect");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang info [gang/player]";
	}

	@Override
	public boolean execute(CommandSender sender, ImmutableList<String> args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.size() == 0) {
				return this.plugin.getGangsManager().sendGangInfo(p, p);
			} else if (args.size() == 1) {
				OfflinePlayer target = Players.getOfflineNullable(args.get(0));

				if (this.plugin.getGangsManager().getPlayerGang(target).isPresent()) {
					return this.plugin.getGangsManager().sendGangInfo(p, target);
				} else {
					return this.plugin.getGangsManager().sendGangInfo(p, args.get(0));

				}
			}
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}
}
