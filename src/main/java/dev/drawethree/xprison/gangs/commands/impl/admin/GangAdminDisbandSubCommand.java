package dev.drawethree.xprison.gangs.commands.impl.admin;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.gangs.utils.GangsConstants;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GangAdminDisbandSubCommand extends GangSubCommand {

	public GangAdminDisbandSubCommand(GangCommand command) {
		super(command, "disband");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 1) {
			Optional<Gang> gangOptional = this.command.getPlugin().getGangsManager().getGangWithName(args.get(0));

			if (!gangOptional.isPresent()) {
				PlayerUtils.sendMessage(sender, this.command.getPlugin().getConfig().getMessage("gang-not-exists"));
				return false;
			}

			return this.command.getPlugin().getGangsManager().forceDisband(sender, gangOptional.get());
		}
		return false;
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang admin disband <player> <gang>";
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