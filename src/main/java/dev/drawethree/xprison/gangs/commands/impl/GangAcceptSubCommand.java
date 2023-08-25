package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.model.Gang;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GangAcceptSubCommand extends GangSubCommand {

	public GangAcceptSubCommand(GangCommand command) {
		super(command, "accept", "join");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang accept <gang>";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender instanceof Player && args.size() == 1) {

			String gangName = args.get(0);
			Optional<Gang> gangOptional = this.command.getPlugin().getGangsManager().getGangWithName(gangName);

			if (!gangOptional.isPresent()) {
				PlayerUtils.sendMessage(sender, this.command.getPlugin().getConfig().getMessage("gang-not-exists"));
				return false;
			}

			return this.command.getPlugin().getGangsManager().acceptInvite((Player) sender, gangOptional.get());
		}
		return false;
	}

	@Override
	public boolean canExecute(CommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabComplete() {
		return new ArrayList<>();
	}
}
