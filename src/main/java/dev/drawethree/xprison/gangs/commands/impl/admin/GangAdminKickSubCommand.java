package dev.drawethree.xprison.gangs.commands.impl.admin;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.utils.GangsConstants;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class GangAdminKickSubCommand extends GangSubCommand {

	public GangAdminKickSubCommand(GangCommand command) {
		super(command, "kick", "remove");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 1) {
			Player target = Players.getNullable(args.get(0));
			return this.command.getPlugin().getGangsManager().forceRemove(sender, target);
		}
		return false;
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang admin kick <player>";
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return sender.hasPermission(GangsConstants.GANGS_ADMIN_PERM);
	}

	@Override
	public List<String> getTabComplete() {
		return Players.all().stream().map(Player::getName).collect(Collectors.toList());
	}
}