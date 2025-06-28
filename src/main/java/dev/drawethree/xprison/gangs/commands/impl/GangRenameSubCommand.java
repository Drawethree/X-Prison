package dev.drawethree.xprison.gangs.commands.impl;

import dev.drawethree.xprison.gangs.commands.GangCommand;
import dev.drawethree.xprison.gangs.commands.GangSubCommand;
import dev.drawethree.xprison.gangs.enums.GangRenameResult;
import dev.drawethree.xprison.gangs.model.GangImpl;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GangRenameSubCommand extends GangSubCommand {

	public GangRenameSubCommand(GangCommand command) {
		super(command, "rename");
	}

	@Override
	public String getUsage() {
		return ChatColor.RED + "/gang rename [new_name]";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.size() == 1 && sender instanceof Player) {
			Player p = (Player) sender;
			String newName = args.get(0);

			Optional<GangImpl> gangOptional = this.command.getPlugin().getGangsManager().getPlayerGang(p);

			if (!gangOptional.isPresent()) {
				PlayerUtils.sendMessage(p, this.command.getPlugin().getConfig().getMessage("not-in-gang"));
				return false;
			}

			GangImpl gangImpl = gangOptional.get();

			if (!gangImpl.isOwner(p)) {
				PlayerUtils.sendMessage(p, this.command.getPlugin().getConfig().getMessage("gang-not-owner"));
				return false;
			}

			return this.command.getPlugin().getGangsManager().renameGang(gangImpl, newName, p);
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
