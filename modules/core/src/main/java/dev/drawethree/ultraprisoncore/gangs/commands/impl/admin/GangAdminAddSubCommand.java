package dev.drawethree.ultraprisoncore.gangs.commands.impl.admin;

import dev.drawethree.ultraprisoncore.gangs.commands.GangCommand;
import dev.drawethree.ultraprisoncore.gangs.commands.GangSubCommand;
import dev.drawethree.ultraprisoncore.gangs.model.Gang;
import dev.drawethree.ultraprisoncore.gangs.utils.GangsConstants;
import me.lucko.helper.utils.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GangAdminAddSubCommand extends GangSubCommand {

	public GangAdminAddSubCommand(GangCommand command) {
		super(command, "add");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 2) {
			Player target = Players.getNullable(args.get(0));
			Optional<Gang> gangOptional = this.command.getPlugin().getGangsManager().getGangWithName(args.get(1));
			return this.command.getPlugin().getGangsManager().forceAdd(sender, target, gangOptional);
		}
		return false;
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang admin add <player> <gang>";
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