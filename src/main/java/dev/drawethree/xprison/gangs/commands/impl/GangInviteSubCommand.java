package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class GangInviteSubCommand extends GangSubCommand {

	public GangInviteSubCommand(GangCommand command) {
		super(command, "invite", "inv");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang invite [player]";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player && args.size() == 1) {
			Player p = (Player) sender;
			Player target = Players.getNullable(args.get(0));
			return this.command.getPlugin().getGangsManager().invitePlayer(p, target);
		}
		return false;
	}


	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabComplete() {
		return Players.all().stream().map(Player::getName).collect(Collectors.toList());
	}
}
