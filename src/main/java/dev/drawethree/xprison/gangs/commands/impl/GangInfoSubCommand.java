package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.model.Gang;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class GangInfoSubCommand extends GangSubCommand {

	public GangInfoSubCommand(GangCommand command) {
		super(command, "info", "inspect");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang info <gang/player>";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.size() == 0) {
				return this.command.getPlugin().getGangsManager().sendGangInfo(p, p);
			} else if (args.size() == 1) {
				OfflinePlayer target = Players.getOfflineNullable(args.get(0));

				if (this.command.getPlugin().getGangsManager().getPlayerGang(target).isPresent()) {
					return this.command.getPlugin().getGangsManager().sendGangInfo(p, target);
				} else {
					return this.command.getPlugin().getGangsManager().sendGangInfo(p, args.get(0));
				}
			}
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabComplete() {
		List<String> tabComplete = new ArrayList<>();
		tabComplete.addAll(Players.all().stream().map(Player::getName).collect(Collectors.toList()));
		tabComplete.addAll(this.command.getPlugin().getGangsManager().getAllGangs().stream().map(Gang::getName).collect(Collectors.toList()));
		return tabComplete;
	}
}
