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

public final class GangAdminJoinSubCommand extends GangSubCommand {

	public GangAdminJoinSubCommand(GangCommand command) {
		super(command, "join", "add");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 2) {
			Player target = Players.getNullable(args.get(0));
			Optional<Gang> gangOptional = this.command.getPlugin().getGangsManager().getGangWithName(args.get(1));

			if (!gangOptional.isPresent()) {
				PlayerUtils.sendMessage(sender, this.command.getPlugin().getConfig().getMessage("gang-not-exists"));
				return false;
			}

			return this.command.getPlugin().getGangsManager().forceAdd(sender, target, gangOptional.get());
		}
		return false;
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang admin join <player> <gang>";
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